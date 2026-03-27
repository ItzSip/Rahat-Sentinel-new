"""
Backend API — FastAPI Entry Point
===================================
Bridge layer between Sentinel AI predictions and Rahat mobile app.
"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

from .routes import alerts


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Startup and shutdown events."""
    print("🚀 Backend API starting up...")
    # TODO: Load ML model on startup
    # TODO: Initialize database connection
    yield
    print("👋 Backend API shutting down...")


app = FastAPI(
    title="Rahat Sentinel API",
    description="Disaster prediction bridge between Sentinel AI and Rahat mobile app",
    version="0.1.0",
    lifespan=lifespan,
)

# CORS — allow mobile app to connect
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Restrict in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register routes
app.include_router(alerts.router, prefix="/api/v1", tags=["alerts"])


@app.get("/health")
async def health_check():
    """Health check endpoint."""
    return {
        "status": "healthy",
        "service": "rahat-sentinel-api",
        "version": "0.1.0",
    }


@app.get("/")
async def root():
    """Root endpoint."""
    return {
        "message": "Rahat Sentinel API",
        "docs": "/docs",
        "health": "/health",
    }
