package itdlp.impl.srv.resources;

import itdlp.api.service.HelloWorld;

public class App implements HelloWorld
{

    @Override
    public String helloWorld() {
        return "Hello-world!!!";
    }
    
}