package dluck.fuckreport.service;

import dluck.fuckreport.dao.UserRepository;
import dluck.fuckreport.domain.User;
import dluck.fuckreport.vo.MailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.Objects;

@Service
public class MailService {

	private final JavaMailSenderImpl mailSender;//注入邮件工具类

	@Autowired
	public MailService(JavaMailSenderImpl mailSender) {
		this.mailSender = mailSender;
	}

	//发送自定义邮件
	public void sendSimpleEmail(String email, String subject, String data) {
		MailVo mailVo = new MailVo();
		mailVo.setTo(email);
		mailVo.setSubject(subject);
		mailVo.setText(data);
		sendMail(mailVo);
	}

	//发送打卡成功邮件
	public void sendReportSuccessEmail(String email, String data) {
		MailVo mailVo = new MailVo();
		mailVo.setTo(email);
		mailVo.setSubject("自动打卡成功！");
		mailVo.setText("打卡成功！发送到服务器的数据如下：\n" + data);
		sendMail(mailVo);
	}

	//发送打卡失败邮件
	public void sendReportFailedEmail(String email, String message) {
		MailVo mailVo = new MailVo();
		mailVo.setTo(email);
		mailVo.setSubject("自动打卡失败！");
		mailVo.setText("打卡失败！失败原因：\n" + message);
		sendMail(mailVo);
	}

	//检测邮件信息类
	private void checkMail(MailVo mailVo) {
		if (StringUtils.isEmpty(mailVo.getTo())) {
			throw new RuntimeException("邮件收信人不能为空");
		}
		if (StringUtils.isEmpty(mailVo.getSubject())) {
			throw new RuntimeException("邮件主题不能为空");
		}
		if (StringUtils.isEmpty(mailVo.getText())) {
			throw new RuntimeException("邮件内容不能为空");
		}
	}

	//构建复杂邮件信息类
	private void sendMail(MailVo mailVo) {
		try {
			MimeMessageHelper messageHelper = new MimeMessageHelper(mailSender.createMimeMessage(), true);//true表示支持复杂类型
			mailVo.setFrom(getMailSendFrom()); //邮件发信人从配置项读取
			messageHelper.setFrom(mailVo.getFrom()); //邮件发信人
			messageHelper.setTo(mailVo.getTo().split(",")); //邮件收信人
			messageHelper.setSubject(mailVo.getSubject()); //邮件主题
			messageHelper.setText(mailVo.getText()); //邮件内容
			if (!StringUtils.isEmpty(mailVo.getCc())) { //抄送
				messageHelper.setCc(mailVo.getCc().split(","));
			}
			if (!StringUtils.isEmpty(mailVo.getBcc())) { //密送
				messageHelper.setCc(mailVo.getBcc().split(","));
			}
			if (mailVo.getMultipartFiles() != null) { //添加邮件附件
				for (MultipartFile multipartFile : mailVo.getMultipartFiles()) {
					messageHelper.addAttachment(Objects.requireNonNull(multipartFile.getOriginalFilename()), multipartFile);
				}
			}
			if (StringUtils.isEmpty(mailVo.getSentDate())) {//发送时间
				mailVo.setSentDate(new Date());
				messageHelper.setSentDate(mailVo.getSentDate());
			}
			mailSender.send(messageHelper.getMimeMessage());//正式发送邮件
			mailVo.setStatus("ok");
		} catch (Exception e) {
			throw new RuntimeException(e);//发送失败
		}
	}

	//获取邮件发信人
	public String getMailSendFrom() {
		return mailSender.getJavaMailProperties().getProperty("from");
	}
}