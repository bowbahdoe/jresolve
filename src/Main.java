public class Main {
    public static void main(String[] args) {
        String searchItem;
        if (args.length == 0) {
            searchItem = "guice";
        } else {
            searchItem = args[0];
        }

        MavenRepo repo = new MavenRepo();
        System.out.println((repo.search(searchItem)));
    }
}
