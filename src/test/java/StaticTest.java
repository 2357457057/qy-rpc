import top.yqingyu.common.utils.UnsafeUtil;

public class StaticTest {
    public static void main(String[] args) throws InstantiationException, NoSuchFieldException {
        Object o = UnsafeUtil.allocateInstance(A.class);
        long l = UnsafeUtil.UNSAFE.objectFieldOffset(A.class.getDeclaredField("abc"));
        System.out.println(o);
        B b = new B();
        b.a = "哈哈哈哈哈哈";
        UnsafeUtil.putObject(o, l, b);
        System.out.println(o);
        System.out.println(((B) ((Object) ((A) o).getAbc())).a);
        System.out.println(((A) o).getAbc() + 1);

    }

    static class A {
        public final Integer abc;
        String a;
        int b;
        Integer c;
        public A(Integer abc) {
            this.abc = abc;
        }
        public Integer getAbc() {
            return abc;
        }
        @Override
        public String toString() {
            return abc + "";
        }
    }

    static class B {
        String a;
        int b;
        Integer c;
    }
}

