package com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ConstructedHTTPMetadata;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.Routes.Path;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.Routes.RoutesLoader;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.parsers.requests.HTTPRequestParser;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.parsers.requests.HTTPRequestParserType;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.parsers.requests.HTTPRequestsParsers;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.parsers.responses.HTTPResponseParser;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.parsers.responses.HTTPResponseParserType;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.parsers.responses.HTTPResponsesParsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HTTPConfig {
    private static final Logger logger = LoggerFactory.getLogger(HTTPConfig.class);
    String thisId = toString().substring(toString().indexOf("@"));

    private final RoutesLoader routesLoader;
    private final Map<HTTPResponsesParsers, HTTPResponseParser> responsesParsers = new HashMap<>();
    private final Map<HTTPRequestsParsers, HTTPRequestParser> requestsParsers = new HashMap<>();

    public HTTPConfig(List<HTTPResponseParser> responseParserList, List<HTTPRequestParser> requestParserList, RoutesLoader routesLoader) {

        for (HTTPResponseParser parser : responseParserList) {
            HTTPResponseParserType annotation = parser.getClass().getAnnotation(HTTPResponseParserType.class);

            if (annotation != null)
                responsesParsers.put(annotation.value(), parser);
        }

        for (HTTPRequestParser parser : requestParserList) {
            HTTPRequestParserType annotation = parser.getClass().getAnnotation(HTTPRequestParserType.class);

            if (annotation != null)
                requestsParsers.put(annotation.value(), parser);
        }

        this.routesLoader = routesLoader;

        logger.info("{} - Initialized HTTPConfig with {} request parsers and {} response parsers.",
                thisId, requestsParsers.size(), responsesParsers.size());
    }


    public HTTPDefinition getHTTPDefinition(String method, String path) throws Exception {
        Path route = routesLoader.getRoute(method, path);

        if (route == null)
            throw new Exception("No route found for path: " + path + " and method: " + method);

        HTTPRequestsParsers requestParserType = HTTPRequestsParsers.from(route.getRequestSchema());
        HTTPRequestParser requestParser = requestsParsers.get(requestParserType);

        HTTPResponsesParsers reponseParser = HTTPResponsesParsers.from(route.getResponseSchema());
        HTTPResponseParser responseParser = responsesParsers.get(reponseParser);

        if (requestParser == null || responseParser == null)
            throw new Exception("No parser found for request schema: " + route.getRequestSchema() + " or response schema: " + route.getResponseSchema());

        return new HTTPDefinition(requestParser, responseParser);
    }
}
