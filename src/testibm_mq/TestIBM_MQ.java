/**
 * PLEASE READ THIS ONE FIRST:
 *      Disable CHLAUTHEN (channel authentication)
 *        cmd > runmqsc [Queue Manager name] > ALTER QMGR CHLAUTH(DISABLED) > end
 *      Because IBM WMQ uses 2 types of connection authen is IDPWOS(uses the local 
 *      operating system to authenticate the user ID and password) and IDPWLDAP
 *      (uses an LDAP server to authenticate the user ID and password), default 
 *      is IDPWOS sothat you use [YOUR LOGIN NAME] or with @domain and your login 
 *      password to continue
 */
package testibm_mq;

import com.ibm.jms.JMSTextMessage;
import com.ibm.mq.jms.JMSC;
import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQQueueConnection;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.MQQueueReceiver;
import com.ibm.mq.jms.MQQueueSender;
import com.ibm.mq.jms.MQQueueSession;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;

/**
 *
 * @author theanh@ilovex.co.jp
 */
public class TestIBM_MQ {
    
    public static final String  HOST_NAME           = "localhost";
    public static final int     PORT_NUMBER         = 1313;
    public static final String  QUEUE_MANAGER_NAME  = "QM_TEST";
    public static final String  QUEUE_NAME          = "Q1";
    public static final String  LOGIN_USERNAME      = "theanh";
    public static final String  LOGIN_PASSWORD      = "GMAIL";
    
    // Virtual Server
//    public static final String  HOST_NAME           = "10.1.231.33";
//    public static final int     PORT_NUMBER         = 1414;
//    public static final String  QUEUE_MANAGER_NAME  = "QM_DEFAULT";
//    public static final String  QUEUE_NAME          = "Q1";
//    public static final String  LOGIN_USERNAME      = "daubac402";
//    public static final String  LOGIN_PASSWORD      = "GMAIL";
    
    public static void main(String[] args) {
        try {
            MQQueueConnectionFactory cf = new MQQueueConnectionFactory();
            cf.setHostName(HOST_NAME);
            cf.setPort(PORT_NUMBER);
            cf.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);
            cf.setQueueManager(QUEUE_MANAGER_NAME);
            cf.setChannel("SYSTEM.DEF.SVRCONN");

            MQQueueConnection connection = (MQQueueConnection) cf.createQueueConnection(LOGIN_USERNAME, LOGIN_PASSWORD);
            MQQueueSession session = (MQQueueSession) connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            MQQueue queue = (MQQueue) session.createQueue(QUEUE_NAME);
            MQQueueSender sender = (MQQueueSender) session.createSender((Queue) queue);
            sender.setPriority(0); //if not, default is 4
            sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT); //DeliveryMode.PERSISTENT, the default
            MQQueueReceiver receiver = (MQQueueReceiver) session.createReceiver((Queue) queue);

            long uniqueNumber = System.currentTimeMillis() % 1000;
            JMSTextMessage message = (JMSTextMessage) session.createTextMessage("random number: " + uniqueNumber);

            // Start the connection
            connection.start();

            // try to send 1 test message
            sender.send(message);
            System.out.println("Sent message:" + message);

            // and then get the first message from MQ
            JMSTextMessage receivedMessage = (JMSTextMessage) receiver.receive();
            System.out.println("Received message:" + receivedMessage);
            
            sender.close();
            receiver.close();
            session.close();
            connection.close();
            System.out.println("DONE");
        } catch (JMSException jmsex) {
            jmsex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
