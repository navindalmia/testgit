package com.test.project.RatpackTestProjject;

import java.util.Collection;
import java.util.HashSet;

import ratpack.handling.Context;
import ratpack.jackson.Jackson;
import ratpack.server.PublicAddress;
import ratpack.server.RatpackServer;

/**
 * Hello world!
 *
 */
//public class App 
//{
//    public static void main( String[] args ) throws Exception
//    {
//        System.out.println( "Hello World!" );
//        RatpackServer.start(server -> server.handlers(chain -> chain
//                .get(ctx -> ctx.render("Welcome to  ratpack!!!"))));
//    }
//}

public class App {

    private static final String ID = "id";
    private Collection<Book> booksRepo = new HashSet<>();

    public static void main(String[] args) throws Exception {
        new App().runServer();
    }

    private void runServer() throws Exception {
        RatpackServer.start(serverDefinition -> serverDefinition
                .handlers(handler -> handler
                        .path("books", ctx -> ctx.byMethod(action -> action.get(this::listBooks)
//                		 .path("books", ctx -> ctx.byMethod(action -> action.get(ctx-> ctx.render(Jackson.json(booksRepo)))
                                .put(this::saveBook)))
                        .path("books/:" + ID, ctx -> ctx.byMethod(
                                action -> action.get(this::getBook)
                                        .post(this::updateBook)
                                        .delete(this::removeBook)))));
    }

    private void listBooks(Context ctx) {
        ctx.render(Jackson.json(booksRepo));
        /*test comment*/
    }

    private void saveBook(Context ctx) {
        ctx.parse(Book.class)
                .onError(error -> ctx.getResponse().status(500)
                        .send(error.getMessage()))
                .then(book -> {
                    booksRepo.add(book);
                    respondWith201(ctx, book.id);
                });
    }

    private void respondWith201(Context ctx, long bookId) {
        PublicAddress url = ctx.get(PublicAddress.class);
        ctx.getResponse().getHeaders()
                .set("Location",
                        url.builder().path("books/" + bookId).build());
        ctx.getResponse().status(201).send();
    }

    private void getBook(Context ctx) {
        int id = ctx.getPathTokens().asInt(ID);
        booksRepo.stream()
                .filter(book -> book.id == id)
                .findFirst()
                .ifPresentOrElse(book -> ctx.render(Jackson.json(book)),
                        () -> ctx.getResponse().status(404)
                                .send("Not found"));
    }

    private void updateBook(Context ctx) {
        int id = ctx.getPathTokens().asInt(ID);
        booksRepo.stream().filter(book -> book.id == id)
                .findFirst()
                .ifPresentOrElse(book -> {
                            booksRepo.remove(book);
                            saveBook(ctx);
                        },
                        () -> ctx.getResponse().status(404)
                                .send("Not found"));
    }

    private void removeBook(Context ctx) {
        int id = ctx.getPathTokens().asInt(ID);
        booksRepo.removeIf(book -> book.id == id);
        ctx.getResponse().status(204).send();
    }

    public static class Book {
        private long id;
        private String author;
        private String title;

        //getters, setters and equals/hashCode methods ommited
    }
}
