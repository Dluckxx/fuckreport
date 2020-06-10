package dluck.fuckreport;

import dluck.fuckreport.domain.User;
import dluck.fuckreport.service.MailService;
import dluck.fuckreport.service.MainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {
	private final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
	private final MainService mainService;
	private final MailService mailService;

	@Autowired
	public ScheduledTasks(MainService mainService, MailService mailService) {
		this.mainService = mainService;
		this.mailService = mailService;
	}

	@Scheduled(cron = "0 1 0-10 * * *")
	public void doReportAll() {
		logger.info("===>doReportAll: 自动打卡定时任务启动！");
		int cnt = 0;
		for (User user : mainService.getAllUser()) {
			String status = mainService.check(user);
			if (status.equals("未打卡")) {
				mainService.report(user);
				cnt++;
			} else if (status.equals("当前采集日期已登记！")) {
				cnt++;
			} else {
				logger.warn("===>doReportAll: 打卡状态异常，用户{}，状态信息{}",
						user.getName() + "--" + user.getUid(), status);
			}
		}
		if (cnt == mainService.getAllUser().size()) {
			logger.info("===>doReportAll: 打卡任务完毕！一切正常，{}个用户已经打卡完成！", cnt);
		} else {
			logger.warn("===>doReportAll: 打卡任务完毕！出现异常，{}个用户已经打卡完成！还有{}个用户未完成打卡！",
					cnt, mainService.getAllUser().size() - cnt);
		}
	}

	@Scheduled(cron = "0 0 8 * * *")
	public void statusReport() {
		logger.info("===>doReportAll: 打卡状态汇报邮件发送任务启动！");
		int cnt = 0;
		StringBuilder data = new StringBuilder("有用户打卡失败！");
		for (User user : mainService.getAllUser()) {
			if (!mainService.check(user).equals("当前采集日期已登记！")) {
				cnt++;
				data.append(user.getName())
						.append("[")
						.append(user.getUid())
						.append("]：")
						.append(mainService.check(user));
			}
		}
		if (cnt != 0) {
			data.append(cnt).append("个用户打卡失败");
			mailService.sendSimpleEmail("dutiesheng@outlook.com",
					"[打卡情况汇报]异常！",
					data.toString());
		} else {
			mailService.sendSimpleEmail("dutiesheng@outlook.com",
					"[打卡情况汇报]全部成功！",
					"一切正常，你这个系统太牛逼了！");
		}
	}
}