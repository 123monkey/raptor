# Change Log

## 0.2.0-RC9 **(2018/8/17)**
* 添加了对 `ControllerAdvice` 支持,在 `RaptorHandlerMethodProcessor` 中注入了 controllerAdvices,以支持 Spring 自带的 `AbstractJsonpResponseBodyAdvice`

 * 对于 bug: "tDouble 这样子的字段在序列化为json的时候会出现两次: tDouble 和 tdouble",之前的修复方式有问题,导致了其他的问题,现在暂时不修复这个bug.


## 0.2.0-RC8 **(2018/8/15)**
* 增加http选项:选择是否使用长连接(raptor.httpclient.keepAlive)

* ~~Fix: tDouble 这样子的字段在序列化为json的时候会出现两次: tDouble 和 tdouble~~

* Fix:处理proto文件中没有添加版本,生成的swagger也缺少版本的问题

* Fix:处理RaptorErrorDecoder爆出NPE的问题



## 0.2.0-RC7 **(2018/8/7)**
* 保留Spring MVC 默认的3个 `HandlerExceptionResolver`,`RaptorHandlerExceptionResolver` 在他们之前执行

* 增加 `raptor.exception.clientOnly` 开关,控制 `RaptorHandlerExceptionResolver` 只处理 Raptor client 请求过来产生的异常

* 去掉 `RaptorHandlerMappingPostProcessor`，暂时用不上

* Fix 修改生成swagger.json中的field的顺序

* Fix 相同 path 不同 method 不显示的 bug

## 0.2.0-RC6 **(2018/7/9)**
* 去掉HeaderTraceRequestInterceptor注册bean，避免干扰spring cloud feign

## 0.2.0-RC5 **(2018/7/6)**
* 修复ConditionalOnMissingBean存在的潜在问题，注解参数不填的话，默认检查方法名称，如果方法名称相同，可能会忽略创建bean，导致需要创建的bean没有创建

## 0.2.0-RC4 **(2018/7/5)**
* 重构benchmark

* codegen增加JsonProperty注解

* 增加ProtoFileEndpoint
