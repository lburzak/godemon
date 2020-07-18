package com.polydome.godemon.discordbotapp;

import com.polydome.godemon.discordbotapp.di.Config;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
        GodemonBot app = context.getBean(GodemonBot.class);
        app.init().subscribe();
    }
}
