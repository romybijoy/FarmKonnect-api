FROM python:3.11-slim

# Prevent Python from writing .pyc files
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV TRANSFORMERS_CACHE=/app/cache

WORKDIR /app

# Install minimal required system libs (for torch + Pillow)
RUN apt-get update && apt-get install -y \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy only requirements first (better layer caching)
COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY ai_grpc_server.py .
COPY ai_pb2.py .
COPY ai_pb2_grpc.py .

EXPOSE 50051

CMD ["python", "ai_grpc_server.py"]
