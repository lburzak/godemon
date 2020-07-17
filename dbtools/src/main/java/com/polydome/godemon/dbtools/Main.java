package com.polydome.godemon.dbtools;

import com.polydome.godemon.dbtools.app.DBTools;
import com.polydome.godemon.dbtools.di.Config;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
        JDA jda = context.getBean(JDA.class);
        DBTools app = context.getBean(DBTools.class);
        app.populateDatabase()
                .subscribe(jda::shutdown);
    }
}
