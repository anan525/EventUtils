# EventUtils
register()
   1.获取当前activity或者fragment的class字节
   2.反射获取@Subscribe注解的方法，并做安全判断（参数，修饰符）
   3.获取@Subscribe中的threadmode,provity,sticky，方法名method等参数
   4.将参数封装成一个subscription存入hashmap中
unregister()
   1.使用当前activity或者fragment的class字节
   2.从hashmap中将对应class字节的key删除
post(msg)
   1.遍历对应的hashmap，取出scription
   2.调用postToScription进行线程调度
   3.出现4种情况:主线程对子线程，子线程对主线程，主线程对主线程，子线程对子线程
       主对主，子对子  ，取出method和class字节  调用method.invoke
       主线程对子线程,  使用的是线程池来执行method.invoke(),线程池是在event.getdefault(),构造函数中初始化
       子线程对主线程，使用的是handler-message消息循环机制，handler.post()方法执行的method.invoke()
