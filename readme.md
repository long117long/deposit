
# 此框架可以在Android开发时，不用写aidl，直接写接口，就能实现跨进程通讯。

## 如何使用：

### 1. 服务端编代码编写如下：

	//↓----------------------------------------------------------------------------------------↓
	 a. 服务端定义的接口类，	 
        public interface IFun {        
                byte[] genRandom(int len);                
        }        
        
	 b. 服务端接口类的实现	  
    //如果使用@ServiceName这个注解的话，在客户端创建代理时setServiceName就要用这个名字。    
	//也可以不使用注解，客户端创建代理时就不用调用setServiceName方法了	
    @ServiceName("Fun")
    public class FunImpl implements IFun {    
        @Override
        public byte[] genRandom(int len) {        
            Random random = new Random();            
            byte[] result = new byte[len];            
            random.nextBytes(result);            
            return result;            
        }        
    }
    
	 c. 使用:
	   (1)如果想托管FunImpl这个"服务"，可以让工程依赖deposit_jar(api project(:deposit_manager))，使用：

            DepositForService.getInstance(getApplicationContext()).register(new FunImpl(), IFun.class);

       (2)如果不想托管FunImpl这个"服务"，只想把FunImpl变成一个Binder，只需让工程依赖deposit(implementation project(:deposit))

             Binder binder  = new DepositBinderService(context, new FunImpl(), IFun.class);

	//↑----------------------------------------------------------------------------------------↑


### 2. 客户端编写代码如下：

	//↓----------------------------------------------------------------------------------------↓
	a. 使用服务端定义的接口类，

        public interface IFun {        
                byte[] genRandom(int len);                
            }

     b. 创建接口代理
        (1)如果在服务端托管了FunImpl这个"服务"，则也需要在此让工程依赖deposit_jar(api project(:deposit_manager))，使用：

        IFun iFun = new DepositForClient.Builder()
                     .setContext(getApplicationContext())
                     .setServiceName("Fun")
                     .create(IFun.class);

        (2)如果在服务端只是让FunImpl这个服务"变成了"Binder，而且在客户端是得到的Binder的代理对象(比如为iBinder)，要这么使用：

        IFun iFun =  new DepositBinderProxy(context, IFun.class, iBinder)
                        .genProxy();

     c. 使用接口类代理

        byte[] random = iFun.genRandom(16);
        
	//↑----------------------------------------------------------------------------------------↑

## 其他：
    可参看demo/OneApp 的示例代码