package com.ecommerce;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // this will run only once when the application starts for data insert
        if (productRepository.count() == 0) {
            System.out.println("Seeding database with 20 dummy products...");

            List<Product> dummyProducts = Arrays.asList(
                    createProduct("Wireless Noise Cancelling Headphones", "Experience pure audio with our premium over-ear headphones featuring active noise cancellation.", 299.99, 50, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80"),
                    createProduct("Ultra-Slim Smartwatch", "Stay connected with this elegant smartwatch tracking your fitness, sleep, and app notifications.", 199.50, 120, "https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=500&q=80"),
                    createProduct("Professional DSLR Camera", "Capture stunning high-resolution photos and 4K videos with this robust professional camera body.", 1299.00, 15, "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=500&q=80"),
                    createProduct("Minimalist Leather Backpack", "A sleek, handcrafted leather backpack perfect for daily commutes and carrying laptops up to 15 inches.", 145.00, 30, "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=500&q=80"),
                    createProduct("Mechanical Gaming Keyboard", "Enhance your gaming performance with tactile mechanical switches and customizable RGB backlighting.", 89.99, 85, "https://images.unsplash.com/photo-1595225476474-87563907a212?w=500&q=80"),
                    createProduct("Ergonomic Office Chair", "Designed for extreme comfort during long hours of work, featuring adjustable lumbar support.", 249.00, 20, "https://images.unsplash.com/photo-1505843490538-5133c6c7d0e1?w=500&q=80"),
                    createProduct("4K Ultra HD Monitor", "27-inch stunning 4K monitor boasting 99% sRGB color accuracy for design professionals.", 350.00, 40, "https://images.unsplash.com/photo-1527443195645-1133f7f28990?w=500&q=80"),
                    createProduct("Smartphone Gimbal Stabilizer", "Record cinematic, shake-free videos on your smartphone with this 3-axis motorized gimbal.", 129.99, 60, "https://images.unsplash.com/photo-1581454508922-0a4b568fb110?w=500&q=80"),
                    createProduct("High-Speed Portable SSD", "1TB blazing fast external solid state drive perfect for editing videos on the go.", 99.00, 100, "https://images.unsplash.com/photo-1536643666014-a9d949cf0b5d?w=500&q=80"),
                    createProduct("Bluetooth Portable Speaker", "Waterproof rugged bluetooth speaker with immersive 360-degree sound and 12-hour battery life.", 59.90, 75, "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=500&q=80"),
                    createProduct("Smart Home Hub", "Control your lights, locks, and thermostat via voice activation with this compact smart hub.", 89.00, 200, "https://images.unsplash.com/photo-1558089687-f282ffcbc126?w=500&q=80"),
                    createProduct("Wireless Charging Pad", "Sleek metallic Qi-certified 15W fast wireless charging pad for iOS and Android devices.", 29.99, 150, "https://images.unsplash.com/photo-1622445272461-c6580cab8755?w=500&q=80"),
                    createProduct("Ceramic Coffee Mug", "Handmade 14oz ceramic mug with a beautiful matte glaze for your morning coffee routines.", 18.50, 300, "https://images.unsplash.com/photo-1514733670139-4d87a1941d55?w=500&q=80"),
                    createProduct("Aviator Sunglasses", "Classic polarized aviator sunglasses ensuring UV protection and timeless style.", 125.00, 60, "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=500&q=80"),
                    createProduct("Fitness Resistance Bands", "Set of 5 heavy-duty resistance bands allowing for a full body workout from home.", 24.99, 400, "https://images.unsplash.com/photo-1598289431512-b97b0a1a36ca?w=500&q=80"),
                    createProduct("Yoga Mat", "Non-slip eco-friendly yoga mat providing optimum cushioning for sensitive joints.", 35.00, 90, "https://images.unsplash.com/photo-1600881333168-2ef49b341f30?w=500&q=80"),
                    createProduct("Drone with 4K Camera", "Foldable drone with remote controller featuring auto-return logic and crisp 4K aerial mapping.", 499.00, 25, "https://images.unsplash.com/photo-1507582020474-9a35b7d455d9?w=500&q=80"),
                    createProduct("Organic Cotton T-Shirt", "Premium fitted crewneck t-shirt made seamlessly from 100% organic cotton for breathability.", 22.00, 150, "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500&q=80"),
                    createProduct("Smart LED Bulb", "Multicolor WiFi-enabled smart bulb. Automate lighting schedules and choose from 16 million colors.", 15.99, 250, "https://images.unsplash.com/photo-1550989460-0adf9ea622e2?w=500&q=80"),
                    createProduct("Stainless Steel Water Bottle", "Insulated leak-proof water bottle that keeps liquids cold for 24 hours or hot for 12 hours.", 28.00, 110, "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=500&q=80")
            );

            productRepository.saveAll(dummyProducts);
            System.out.println("20 products seeded successfully!");
        } else {
            System.out.println("Database already contains products. Skipping seed operation.");
        }
    }

    private Product createProduct(String name, String description, double price, int stockQuantity, String imageUrl) {
        return Product.builder()
                .name(name)
                .description(description)
                .price(BigDecimal.valueOf(price))
                .stockQuantity(stockQuantity)
                .imageUrl(imageUrl)
                .build();
    }
}
