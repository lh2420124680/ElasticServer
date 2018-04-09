#该项目为maven项目

#该代码中只有activeMQ的消息监听服务，消息发送需要另外写

#示例
/**
 * 根据传过来的数据放入队列中插入到相应的索引库中
 * <p>Description: <p>
 * @param data void
 * @date 2018-1-23 上午8:59:30
 */
public void  elasticToIndex(String data){
	String encodeData = java.net.URLEncoder.encode(data);
	jmsService.sendMessage1("elasticsearch", encodeData, "QUEUE");
}



import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import ecp.framework.jms.service.ProducerService;

/**
 * JMS生成者
 * @author jason
 *
 */
public class ProducerServiceImpl implements ProducerService {
	
	private static final String MSG_TYPE_Q = "QUEUE";
	
	private static final String MSG_TYPE_T = "TOPIC";

	private JmsTemplate jmsQueueTemplate;
	
	private JmsTemplate jmsTopicTemplate;

	public void setJmsQueueTemplate(JmsTemplate jmsQueueTemplate) {
		this.jmsQueueTemplate = jmsQueueTemplate;
	}

	public void setJmsTopicTemplate(JmsTemplate jmsTopicTemplate) {
		this.jmsTopicTemplate = jmsTopicTemplate;
	}

	@Override
	public void sendMessage(String destination, String msg) {
		sendMessage1(destination, msg, MSG_TYPE_T);
	}
	
	@Override
	public void sendMessage1(String destination, final String msg, String msgType) 
	{

		if (destination == null || destination.trim().equals("") || msg == null)
		{
			return;
		}
		
		Destination des = null;
		JmsTemplate jmsTemplate = null;
		
		if (MSG_TYPE_T.equalsIgnoreCase(msgType))
		{
			des = new ActiveMQTopic(destination);
			jmsTemplate = jmsTopicTemplate;
		}
		else{
			des = new ActiveMQQueue(destination);
			jmsTemplate = jmsQueueTemplate;
		}

		jmsTemplate.send(des, new MessageCreator() 
		{
			public Message createMessage(Session session) throws JMSException 
			{
				TextMessage text = session.createTextMessage(msg);
				return text;
			}
		});
	}
}


