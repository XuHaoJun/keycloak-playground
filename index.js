const rhea = require("rhea");
const amqp = require("amqplib");

async function listenToRabbitMQChannel() {
  try {
    // Connect to RabbitMQ
    const connection = await amqp.connect("amqp://admin:admin@localhost:5672");
    const channel = await connection.createChannel();

    // Declare the queue
    const queue = "send-email";
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
    console.error("Error:", err);
  }
}

async function listenToRabbitMQChannel2() {
  try {
    const container = rhea.create_container();
    const connection = container.connect({
      host: "localhost",
      port: 5672,
      username: "admin",
      password: "admin",
    });

    connection.open_receiver("send-email");
    console.log("Listening for messages in the send-email queue...");

    connection.on("message", (context) => {
      const message = context.message;
      console.log(`Received message: ${message.body.content.toString()}`);
    });
  } catch (err) {
    console.error("Error:", err);
  }
}

listenToRabbitMQChannel2();
