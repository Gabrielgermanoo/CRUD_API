import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Scanner;

import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class Utils {
    Scanner input = new Scanner(System.in);
    public MongoCollection<Document> connect(){
        try {
            ServerAddress seed1 = new ServerAddress("localhost", 27017);
            MongoClientSettings setting = MongoClientSettings.builder()
                    .applyToClusterSettings(builder -> builder.hosts(Arrays.asList(seed1)))
                    .build();
            MongoClient conn = MongoClients.create(setting);
            MongoDatabase database = conn.getDatabase("jmongo");

            return database.getCollection("products");
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public void disconnect(MongoCursor<Document> cursor){
        cursor.close();
    }
    public void list(){
        MongoCollection<Document> collection = connect();
        if(collection.countDocuments() > 0){
            MongoCursor<Document> cursor = collection.find().iterator();
        try{
            System.out.println("Listing products..." +
                    "--------------");
            while(cursor.hasNext()){
                String json = cursor.next().toJson();
                JSONObject obj = new JSONObject(json);
                JSONObject id = obj.getJSONObject("_id");
                System.out.println("id" + id.get("$oid"));
                System.out.println("Product " + obj.get("name"));
                System.out.println("Price " + obj.get("price"));
                System.out.println("Storage " + obj.get("storage"));
                System.out.println("--------------------------");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        disconnect(cursor);
        }else {
            System.out.println("No registered documents");
        }
    }
    public void insert(){
        MongoCollection<Document> collection = connect();
        input.nextLine();
        System.out.println("Write the name of the product:");
        String name = input.nextLine();
        System.out.println("Write the price of the product:");
        double price = input.nextDouble();
        System.out.println("Write the storage of the product");
        int storage = input.nextInt();
        JSONObject new_product = new JSONObject();
        new_product.put("name", name);
        new_product.put("price", price);
        new_product.put("storage", storage);

        collection.insertOne(Document.parse(new_product.toString()));

        System.out.println("The Product " + name + " was inserted successfully!");
    }
    public void update(){
        MongoCollection<Document> collection = connect();
        System.out.println("Write the id of the product");
        String _id = input.nextLine();
        System.out.println("Write the name of the product");
        String name = input.nextLine();
        System.out.println("Write the price of the product");
        double price = input.nextDouble();
        System.out.println("Write the storage of the product");
        int storage = input.nextInt();
        Bson query = combine(set("name", name), set("price", price), set("storage", storage));

        try {
            UpdateResult result = collection.updateOne(new Document("_id", new ObjectId(_id)), query);

            if (result.getModifiedCount() == 1) {
                System.out.println("The product " + name + " was update successfully");
            } else System.out.println("The product cannot be updated");
        }catch (IllegalArgumentException e){
            System.out.println("Invalid id!");
            e.printStackTrace();
        }
    }
    public void retrieve(){
        MongoCollection<Document> collection = connect();
        String _id = input.nextLine();
        try {
            DeleteResult result = collection.deleteOne(new Document("_id", new ObjectId(_id)));
            if (result.getDeletedCount() == 1) {
                System.out.println("The product was deleted successfully");
            } else System.out.println("The product cannot be deleted");
        }catch (IllegalArgumentException e){
            System.out.println("Invalid id!");
            e.printStackTrace();
        }
    }
    public void menu(){
        loop: while(true) {
            System.out.println("""
                    Product Manager!
                    Select a option:
                    1 - List products
                    2 - Insert products
                    3 - Update products
                    4 - Retrieve products
                    5 - Exit""");
            int option = input.nextInt();
            switch (option) {
                case 1 -> list();
                case 2 -> insert();
                case 3 -> update();
                case 4 -> retrieve();
                case 5 -> {break loop;}
                default -> System.out.println("Invalid option!");
            }
        }
    }
}
