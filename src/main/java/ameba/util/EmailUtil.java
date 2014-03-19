package ameba.util;

import ameba.feature.EmailFeature;
import httl.Engine;
import httl.Template;
import org.apache.commons.mail.*;

import java.io.IOException;
import java.text.ParseException;

/**
 * @author: ICode
 * @since: 13-8-23 下午8:04
 */
public class EmailUtil {
    private static Engine engine;
    private static String directory;

    private EmailUtil() {
    }

    private static void configure(Email email) throws EmailException {
        email.setHostName(EmailFeature.getHostName());
        email.setSmtpPort(EmailFeature.getSmtpPort());
        email.setAuthenticator(new DefaultAuthenticator(EmailFeature.getUserName(), EmailFeature.getUserPassword()));
        email.setSSLOnConnect(EmailFeature.isSSLEnabled());
        email.setFrom(EmailFeature.getFrom());
        email.setCharset(EmailFeature.getTemplateProperties().getProperty("message.encoding"));
    }

    private static String renderTemplate(String tplPath, Object bean) throws IOException, ParseException {

        if (engine == null) {
            synchronized (EmailUtil.class) {
                if (engine == null) {
                    engine = Engine.getEngine("mailTemplate", EmailFeature.getTemplateProperties());
                    directory = EmailFeature.getTemplateProperties().getProperty("directory");
                }
            }
        }


        Template template = engine.getTemplate(directory + tplPath);
        return (String) template.evaluate(bean);
    }

    /**
     * 发送文本邮件
     *
     * @param subject 主题
     * @param message 消息
     * @param tos     收件人
     * @throws EmailException
     */
    public static void sendText(String subject, String message, String[] tos) throws EmailException {
        Email email = new SimpleEmail();

        configure(email);

        email.setSubject(subject);
        email.setMsg(message);
        email.addTo(tos);
        email.send();
    }

    /**
     * 发送文本邮件
     *
     * @param subject
     * @param tpl
     * @param bean
     * @param tos
     * @throws EmailException
     */
    public static void sendText(String subject, String tpl, Object bean, String[] tos) throws EmailException, IOException, ParseException {
        Email email = new SimpleEmail();

        configure(email);

        email.setSubject(subject);
        email.setMsg(renderTemplate(tpl, bean));
        email.addTo(tos);
        email.send();
    }

    /**
     * 发送HTML邮件
     *
     * @param subject
     * @param tpl
     * @param bean
     * @param tos
     * @throws EmailException
     */
    public static void sendHtml(String subject, String tpl, Object bean, String[] tos) throws EmailException, IOException, ParseException {
        HtmlEmail email = new HtmlEmail();

        configure(email);

        email.setSubject(subject);
        email.setHtmlMsg(renderTemplate(tpl, bean));
        email.addTo(tos);
        email.send();
    }

    /**
     * 发送HTML邮件
     *
     * @param subject
     * @param message
     * @param tos
     * @throws EmailException
     */
    public static void sendHtml(String subject, String message, String[] tos) throws EmailException {
        HtmlEmail email = new HtmlEmail();

        configure(email);

        email.setSubject(subject);
        email.setMsg(message);
        email.addTo(tos);
        email.send();
    }
}
