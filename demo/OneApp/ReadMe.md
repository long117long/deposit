#说明
OneApp目录下，模拟的是一个工程结构。用于演示如何使用deposit。
其中oneapp是应用；
OneAppBase里面存放的是接口；
OneAppLibA和OneAppLibB是不同的功能module，都会依赖OneAppBase中的接口。

其中OneAppLibB这个模块的UseFunActivity，会使用到OneAppLibA模块下IAFun接口实现。
在OneAppLibA下面，IAFun有两个接口实现，一个是FunDefault，一个是FunUseServiceName。

FunDefault是最简单的一种默认实现，
而FunUseServiceName，主要是在类上多加了Annotation: @ServiceName("AF")。
但这两个实现，想要被其他地方使用的时候，所谓的注册方式在ServiceA中都是一样的：

    DepositForService.getInstance(getApplicationContext()).register(xxxxx, IAFun.class);

关于调用register，不用非得在Service中进行，这个可以在任意一个地方进行，比如Activity，ContentProvider等等，
只要有地方调用注册方法就可以了。

而在OneAppLibB中使用的时候，
如果要调用的是FunDefault，则只需：

    IAFun defaultFun = new DepositForClient.Builder()
                                       .setContext(getApplicationContext())
                                       .create(IAFun.class);

如果要调用的是FunUseServiceName，会比上面的多一步setServiceName("AF")
其中"AF"，就是FunUseServiceName上加的@ServiceName("AF")中的"AF"。