from shared.clients.celery.celery_app import celery
from asgiref.sync import async_to_sync




@celery.task
def handle_user_like(message):
    async_to_sync(async_handle_user_like)(message)


async def async_handle_user_like(message):
    pass


@celery.task
def handle_user_unlike(message):
    async_to_sync(async_handle_user_unlike)(message)


async def async_handle_user_unlike(message):
    pass