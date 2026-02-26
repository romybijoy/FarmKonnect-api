import grpc
from concurrent import futures
from io import BytesIO
import ai_pb2
import ai_pb2_grpc
import requests
import torch
from transformers import pipeline, CLIPProcessor, CLIPModel
from PIL import Image
import logging

logging.basicConfig(level=logging.INFO)

# ----------------------------
# DEVICE SETUP
# ----------------------------
device = "cuda" if torch.cuda.is_available() else "cpu"
logging.info(f"Using device: {device}")

# ----------------------------
# LOAD MODELS (ONCE AT STARTUP)
# ----------------------------
classifier = pipeline(
    "zero-shot-classification",
    model="typeform/distilbert-base-uncased-mnli",
    device=0 if device == "cuda" else -1
)

clip_model = CLIPModel.from_pretrained("openai/clip-vit-base-patch32")
clip_model.to(device)
clip_model.eval()

clip_processor = CLIPProcessor.from_pretrained("openai/clip-vit-base-patch32")

logging.info("Models loaded successfully")

# Reuse HTTP session for performance
http_session = requests.Session()

CONFIDENCE_THRESHOLD = 0.65


class AIService(ai_pb2_grpc.AIServiceServicer):

    # ----------------------------
    # TEXT VALIDATION
    # ----------------------------
    def ValidateContent(self, request, context):
        labels = ["agriculture related", "not agriculture related"]

        result = classifier(request.text, labels)
        best_label = result["labels"][0]
        best_score = result["scores"][0]

        if best_score < CONFIDENCE_THRESHOLD:
            verdict = "uncertain"
        else:
            verdict = "agri" if best_label == "agriculture related" else "non-agri"

        return ai_pb2.AIResponse(
            verdict=verdict,
            score=float(best_score)
        )

    # ----------------------------
    # IMAGE VALIDATION
    # ----------------------------
    def ValidateImage(self, request, context):

        try:
            response = http_session.get(request.imageUrl, timeout=3)
            response.raise_for_status()

            image = Image.open(BytesIO(response.content)).convert("RGB")

            labels = ["agriculture related", "not agriculture related"]

            inputs = clip_processor(
                text=labels,
                images=image,
                return_tensors="pt",
                padding=True
            )

            inputs = {k: v.to(device) for k, v in inputs.items()}

            with torch.no_grad():
                outputs = clip_model(**inputs)

            logits = outputs.logits_per_image
            probs = logits.softmax(dim=1)

            best_index = probs.argmax().item()
            best_score = probs[0][best_index].item()

            if best_score < CONFIDENCE_THRESHOLD:
                verdict = "uncertain"
            else:
                verdict = "agri" if best_index == 0 else "non-agri"

            return ai_pb2.AIResponse(
                verdict=verdict,
                score=float(best_score)
            )

        except Exception as e:
            logging.error(f"Image validation failed: {e}")
            return ai_pb2.AIResponse(
                verdict="error",
                score=0.0
            )


def serve():
    server = grpc.server(
        futures.ThreadPoolExecutor(max_workers=10)
    )

    ai_pb2_grpc.add_AIServiceServicer_to_server(AIService(), server)

    server.add_insecure_port('[::]:50051')
    server.start()

    logging.info("AI gRPC server running on port 50051")
    server.wait_for_termination()


if __name__ == '__main__':
    serve()
