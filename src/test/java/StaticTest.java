public class StaticTest {
    public static void main(String[] args) throws InstantiationException, NoSuchFieldException {
        System.out.println(new String(new byte[]{38, 38, 45, 113, 121}));
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

