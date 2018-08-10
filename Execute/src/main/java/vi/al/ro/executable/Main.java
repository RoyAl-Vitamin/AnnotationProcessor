package vi.al.ro.executable;

import vi.al.ro.app.SampleIndex;

public class Main {

    public static void main(String... args) {
        System.out.println("Сервисы приложения:");
        for (Class serviceClass : SampleIndex.TYPES_SERVICE) {
            System.out.println(serviceClass.getCanonicalName());
        }
    }
}
