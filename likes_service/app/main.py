import asyncio
import logging
from shared.clients.kafka.kafka_manager import kafka_manager
from shared.clients.kafka.async_kafka_producer import async_kafka_producer
from likes_service.likes.crud import handle_user_like, handle_user_unlike

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
)
logger = logging.getLogger(__name__)



async def setup_and_run():
    logger.info("Запуск сервиса лайков...")
    
    async_kafka_producer.start()
    
    await kafka_manager.add_consumer(
        topic='user_likes',
        group_id='likes_service',
        handler=handle_user_like,
        consumer_id="like_consumer"
    )
    
    await kafka_manager.add_consumer(
        topic='user_unlikes',
        group_id='likes_service',
        handler=handle_user_unlike,
        consumer_id="unlike_consumer"
    )
    
    logger.info("Сервис лайков успешно запущен и ожидает сообщений")
    
    try:
        while True:
            await asyncio.sleep(1)
    except KeyboardInterrupt:
        logger.info("Получен сигнал остановки")
    finally:
        await kafka_manager.stop_all()
        await async_kafka_producer.stop()
        
        logger.info("Сервис лайков остановлен")


if __name__ == "__main__":
    asyncio.run(setup_and_run()) 