package cat.udl.easymodel;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.MathQueue;
import cat.udl.easymodel.thread.NewUserVisitTaskRunnable;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The entry point of the Spring Boot application.
 */

//@NpmPackage(value = "lumo-css-framework", version = "^4.0.10")
//@NpmPackage(value = "line-awesome", version = "1.3.0")
@Theme(value = "easymodel")
@PWA(name = "EasyModel", shortName = "EasyModel")
@Push
@SpringBootApplication
@ServletComponentScan
public class Application implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    @Bean
    public CommandLineRunner CommandLineRunnerBean() {
        return (args) -> {
            System.out.println("STARTING WEBAPP " + SharedData.fullAppName);
            SharedData sharedData = SharedData.getInstance(); // read/create .properties file
            try {
                sharedData.getDbManager().open();
                sharedData.getUsers().loadDB();
                sharedData.getPredefinedFormulas().loadDB();
                sharedData.getGenericFormulas().loadDB();
                new NewUserVisitTaskRunnable().run();
                System.out.println("MYSQL START OK!");
            } catch (Exception e) {
                System.err.println("MYSQL START ERROR!");
            }
            ExecutorService mathQueueExecutorService=Executors.newSingleThreadExecutor();
            mathQueueExecutorService.submit(MathQueue.getInstance());
            System.out.println("ONSTART FINISH");
        };
    }

    @PreDestroy
    public void onExit() {
        System.out.println("STOPPING WEBAPP "+SharedData.fullAppName);
        SharedData sharedData = SharedData.getInstance();
        try {
            sharedData.getDbManager().close();
        } catch (Exception e) {
            System.err.println("MYSQL CLOSE ERROR!");
        }
        MathQueue.getInstance().stopThread();
        for (int i=0;true;i++) {
            if (MathQueue.getInstance().isStoppedThread())
                break;
            if (i>=100)
                break;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("ONEXIT FINISH");
    }
}
