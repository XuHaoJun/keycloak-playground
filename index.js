const amqp = require('amqplib');

async function listenToRabbitMQChannel() {
  try {
    // Connect to RabbitMQ
    const connection = await amqp.connect('amqp://admin:admin@localhost:5672');
    const channel = await connection.createChannel();

    // Declare the queue
    const queue = 'foo';
    await channel.assertQueue(queue, { durable: true });

    // Start consuming messages
    console.log(`Listening for messages in the ${queue} queue...`);
    await channel.consume(queue, (msg) => {
      if (msg !== null) {
        console.log(`Received message: ${msg.content.toString()}`);
        channel.ack(msg);
      }
    });
  } catch (err) {
    console.error('Error:', err);
  }
}

listenToRabbitMQChannel();