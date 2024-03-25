package top.yqingyu.rpc.producer;


public class InterChainTest {
    public static void main(String[] args) {
        QyRpcInterceptorChain qyRpcInterceptorChain = new QyRpcInterceptorChain();
        qyRpcInterceptorChain.add(new I1());
        qyRpcInterceptorChain.add(new I2());
        qyRpcInterceptorChain.add(new I3());
        for (int i = 0; i < 20; i++) {
            int finalI = i;
            new Thread(() -> qyRpcInterceptorChain.doChain(() -> System.out.println( Thread.currentThread().getName() + "ok" + finalI))).start();
        }
    }

    static class I1 implements QyRpcInterceptor {

        @Override
        public void pre() {
            System.out.println(Thread.currentThread().getName()+ ":" + 1);
        }

        @Override
        public void post() {
            System.out.println(Thread.currentThread().getName()+ ":" + 6);

        }
    }


    static class I2 implements QyRpcInterceptor {

        @Override
        public void pre() {
            System.out.println(Thread.currentThread().getName()+ ":" + 2);
        }

        @Override
        public void post() {
            System.out.println(Thread.currentThread().getName()+ ":" + 5);
        }
    }

    static class I3 implements QyRpcInterceptor {

        @Override
        public void pre() {
            System.out.println(Thread.currentThread().getName()+ ":" + 3);
        }

        @Override
        public void post() {
            System.out.println(Thread.currentThread().getName()+ ":" + 4);
        }
    }
}
