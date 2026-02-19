import grpc
from concurrent import futures
from io import BytesIO
import ai_pb2
import ai_pb2_grpc
import requests

from transformers import pipeline, CLIPProcessor, CLIPModel
from PIL import Image
import torch

# Text classifier
classifier = pipeline(
    "zero-shot-classification",
    model="typeform/distilbert-base-uncased-mnli"
)

# Image classifier (CLIP)
clip_model = CLIPModel.from_pretrained("openai/clip-vit-base-patch32")
clip_processor = CLIPProcessor.from_pretrained("openai/clip-vit-base-patch32")


class AIService(ai_pb2_grpc.AIServiceServicer):

    # ----------------------------
    # TEXT VALIDATION
    # ----------------------------
    def ValidateContent(self, request, context):

        labels = ["agriculture related", "not agriculture related"]
        result = classifier(request.text, labels)

        best_label = result["labels"][0]
        best_score = result["scores"][0]

        verdict = "agri" if best_label == "agriculture related" else "non-agri"

        return ai_pb2.AIResponse(
            verdict=verdict,
            score=best_score
        )

    # ----------------------------
    # IMAGE VALIDATION
    # ----------------------------
    def ValidateImage(self, request, context):

        try:
            response = requests.get(request.imageUrl, timeout=5)
            response.raise_for_status()

            image = Image.open(BytesIO(response.content)).convert("RGB")

            labels = ["agriculture related", "not agriculture related"]

            inputs = clip_processor(
                text=labels,
                images=image,
                return_tensors="pt",
                padding=True
            )

            outputs = clip_model(**inputs)
            logits_per_image = outputs.logits_per_image
            probs = logits_per_image.softmax(dim=1)

            best_index = probs.argmax().item()
            best_score = probs[0][best_index].item()

            verdict = "agri" if best_index == 0 else "non-agri"

            return ai_pb2.AIResponse(
                verdict=verdict,
                score=best_score
            )

        except Exception as e:
            print("Image validation failed:", e)

            return ai_pb2.AIResponse(
                verdict="unknown",
                score=0.0
            )



def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    ai_pb2_grpc.add_AIServiceServicer_to_server(AIService(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    print("AI gRPC server running on port 50051")
    server.wait_for_termination()


if __name__ == '__main__':
    serve()
