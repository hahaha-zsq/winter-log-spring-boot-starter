package com.zsq.winter.log.entity;

import com.zsq.winter.log.config.LogProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.util.ObjectUtils;

/**
 * banner创建
 * 是一个接口，常用于项目启动后，（也就是ApringApplication.run()执行结束），立马执行某些逻辑。可用于项目的准备工作，比如加载配置文件，加载执行流，定时任务等等
 * @author zero
 * @date 2024/05/15
 */
@Slf4j
public class BannerCreator implements ApplicationRunner {
    private LogProperties logProperties;

    public BannerCreator(LogProperties logProperties) {
        this.logProperties = logProperties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //https://www.bootschool.net/ascii
        String str=""+
                " ___       __   ___  ________   _________  _______   ________                 ___       ________  ________     \n" +
                "|\\  \\     |\\  \\|\\  \\|\\   ___  \\|\\___   ___\\\\  ___ \\ |\\   __  \\               |\\  \\     |\\   __  \\|\\   ____\\    \n" +
                "\\ \\  \\    \\ \\  \\ \\  \\ \\  \\\\ \\  \\|___ \\  \\_\\ \\   __/|\\ \\  \\|\\  \\  ____________\\ \\  \\    \\ \\  \\|\\  \\ \\  \\___|    \n" +
                " \\ \\  \\  __\\ \\  \\ \\  \\ \\  \\\\ \\  \\   \\ \\  \\ \\ \\  \\_|/_\\ \\   _  _\\|\\____________\\ \\  \\    \\ \\  \\\\\\  \\ \\  \\  ___  \n" +
                "  \\ \\  \\|\\__\\_\\  \\ \\  \\ \\  \\\\ \\  \\   \\ \\  \\ \\ \\  \\_|\\ \\ \\  \\\\  \\\\|____________|\\ \\  \\____\\ \\  \\\\\\  \\ \\  \\|\\  \\ \n" +
                "   \\ \\____________\\ \\__\\ \\__\\\\ \\__\\   \\ \\__\\ \\ \\_______\\ \\__\\\\ _\\               \\ \\_______\\ \\_______\\ \\_______\\\n" +
                "    \\|____________|\\|__|\\|__| \\|__|    \\|__|  \\|_______|\\|__|\\|__|               \\|_______|\\|_______|\\|_______|\n"
                + "\r\n" + WinterLogConstants.DEV_DOC_URL
                + " (" + WinterLogConstants.VERSION_NO + ")";
        if(!ObjectUtils.isEmpty(logProperties.getIsPrint())&&logProperties.getIsPrint()){
            log.info(str);
        }
    }
}
