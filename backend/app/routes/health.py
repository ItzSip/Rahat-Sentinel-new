from fastapi import APIRouter
from app.schemas import HealthCheck

router = APIRouter(tags=["Health"])

@router.get("/health", response_model=HealthCheck)
async def health_check():
    """ Verify the service is running. """
    return {"status": "ok"}
