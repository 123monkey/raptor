package com.ppdai.framework.raptor.spring.service;

import com.ppdai.framework.raptor.common.RaptorConstants;
import com.ppdai.framework.raptor.exception.ErrorMessage;
import com.ppdai.framework.raptor.exception.RaptorException;
import com.ppdai.framework.raptor.rpc.RaptorContext;
import com.ppdai.framework.raptor.rpc.RaptorRequest;
import com.ppdai.framework.raptor.spring.converter.RaptorMessageConverter;
import com.ppdai.framework.raptor.spring.utils.RaptorHandlerUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * @author yinzuolong
 */
@Order(-100)
@Slf4j
public class RaptorHandlerExceptionResolver implements HandlerExceptionResolver {


    @Value("${raptor.exception.clientOnly:false}")
    private boolean clientOnly;

    @Setter
    @Getter
    private RaptorMessageConverter raptorMessageConverter;

    public RaptorHandlerExceptionResolver(RaptorMessageConverter raptorMessageConverter) {
        this.raptorMessageConverter = raptorMessageConverter;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (RaptorHandlerUtils.isRaptorService(handler) && checkClient()) {
            log.error("RaptorHandlerExceptionResolver:",ex);
            response.setStatus(500);
            response.addHeader(RaptorConstants.HEADER_ERROR, "true");
            ServletServerHttpResponse outputMessage = new ServletServerHttpResponse(response);
            ErrorMessage errorMessage = createErrorMessage(ex);
            try {
                raptorMessageConverter.write(errorMessage, getMediaType(), outputMessage);
            } catch (IOException e) {
                log.error("Can't convert error message.", e);
                processConvertError(response, ex);
            }
            return new ModelAndView();
        }
        return null;
    }

    /**
     * @return 如果设置 clientOnly 为 true,当请求为 raptor client 发送时返回 true
     *         如果clientOnly 为 false, 始终返回 true
     */
    private boolean checkClient() {
        if (clientOnly) {
            return isRaptorClient();
        } else {
            return true;
        }
    }

    private boolean isRaptorClient() {
        Optional<String> requestId = Optional.ofNullable(RaptorContext.getContext())
                .map(RaptorContext::getRequest)
                .map(RaptorRequest::getAttachments)
                .map(attachment -> attachment.get(RaptorConstants.HEADER_REQUEST_ID));
        return requestId.isPresent();
    }

    protected MediaType getMediaType() {
        if (!CollectionUtils.isEmpty(raptorMessageConverter.getSupportedMediaTypes())) {
            return raptorMessageConverter.getSupportedMediaTypes().get(0);
        }
        return new MediaType("application", "*+json");
    }

    protected ErrorMessage createErrorMessage(Exception ex) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(ex.getMessage());
        if (ex instanceof RaptorException) {
            RaptorException raptorException = (RaptorException) ex;
            errorMessage.setCode(raptorException.getCode());
            errorMessage.setAttachments(raptorException.getAttachments());
        }
        return errorMessage;
    }

    protected void processConvertError(HttpServletResponse response, Exception ex) {
        if (!response.isCommitted()) {
            try {
                ServletOutputStream os = response.getOutputStream();
                os.print("raptor service error: ");
                os.print(ex.getMessage());
                os.flush();
                os.close();
            } catch (IOException e) {
                log.error("processConvertError error.", e);
            }
        }
    }

}
