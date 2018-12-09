package tn.insat.tpmicro.productservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
public class ProductRestService {
    Logger logger = LoggerFactory.getLogger(ProductRestService.class);

    @Value("${me}")
    private String me;

    @RequestMapping("/messages")
    public String tellMe() throws UnknownHostException {
//        System.out.println("c'est moi qui ait répondu!");
        logger.warn("c'est moi qui ait répondu!");
        return me + " " + InetAddress.getLocalHost().getCanonicalHostName();
    }
}