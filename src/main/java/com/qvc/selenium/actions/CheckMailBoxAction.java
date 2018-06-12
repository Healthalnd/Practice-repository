package com.qvc.selenium.actions;

import com.qvc.selenium.reporting.ExecuteResult;

import javax.mail.*;
import java.io.IOException;
import java.util.Properties;

/*
Verify that a mail with specified text was received by specified mail address. (Tested only for gmail accounts)
Inputs (like "mail=mail@mail.mail"):
    mail - Email address of a box, which should receive new mail.
    pass - password of this email box.
    search - piece of text, which should identify sent email.
 */

//it is you own local ip. for get if execute the command "route print" and get watch Gateway column.
//String yourOwnGatewayIP = "10.25.0.1";

//use it command to add IMAP_GMAIL_HOST_IP for using in Local Internet connection ignoring VPN.
//route add IMAP_GMAIL_HOST_IP yourOwnGatewayIP

public class CheckMailBoxAction extends BaseAction {
    private static final String IMAP_GMAIL_HOST_IP = "64.233.185.109";

    @Override
    public ExecuteResult runAction() throws Exception {
        super.logAction();
        String mail = (String) testData.get("mail");
        String pass = (String) testData.get("pass");
        String searchString = (String) testData.get("search");
        boolean res = checkMail(mail, pass, searchString);
        String msg = res ? ("Found '" + searchString + "' in '" + mail + "' mailBox") : ("'" + searchString + "' in '" + mail + "' mailBox not Found.");
        stepResult.setResult(res);
        stepResult.setStatus(res ? "Passed" : "Failed");
        stepResult.setStepDetail(msg);
        return stepResult;
    }

    private boolean checkMail(String mail, String pass, String searchString) {
        logger.debug("Get all mails from " + mail);
        logger.debug("Search  " + searchString + " in INBOX");
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        boolean res = false;
        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect(IMAP_GMAIL_HOST_IP, mail, pass);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            int msgCount = inbox.getMessageCount();
            for (int mIndx = 1; mIndx <= msgCount; mIndx++) {

                logger.debug("MESSAGE NUMBER " + mIndx);
                Message msg = inbox.getMessage(mIndx);
                Address[] in = msg.getFrom();
                for (Address address : in) {
                    logger.debug("FROM:" + address.toString());
                }
                Multipart mp = (Multipart) msg.getContent();
                logger.debug("SENT DATE:" + msg.getSentDate());
                logger.debug("SUBJECT:" + msg.getSubject());
                logger.debug("CONTENT:");
                for (int i = 0; i < mp.getCount() - 1; i++) {
                    res = res || dumpPart(mp.getBodyPart(i), searchString);
                }
            }
        } catch (Exception e) {
            logger.error("checkMail error: " + e.getMessage());
        }
        return res;
    }

    private boolean dumpPart(Part p, String searchString) throws MessagingException, IOException {
        boolean res = false;
        if (p.isMimeType("text/plain")) {
            String text = p.getContent() + "";
            logger.debug(text);
            System.out.println(text);

            res = text.contains(searchString);
        }

        if (p.isMimeType("message/rfc822")) {
            res = dumpPart((Part) p.getContent(), searchString);
        }


        if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++)
                res = res || dumpPart(mp.getBodyPart(i), searchString);
        }
        return res;
    }

    //For local method testing
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(new CheckMailBoxAction().checkMail("mail.qvc@gmail.com", "qazxswedc321", "Julia"));
    }

}
