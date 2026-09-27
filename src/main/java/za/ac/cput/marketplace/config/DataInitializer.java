package za.ac.cput.marketplace.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import za.ac.cput.marketplace.model.Listing;
import za.ac.cput.marketplace.model.ListingStatus;
import za.ac.cput.marketplace.model.Role;
import za.ac.cput.marketplace.model.User;
import za.ac.cput.marketplace.repository.ListingRepository;
import za.ac.cput.marketplace.repository.UserRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   ListingRepository listingRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                return; // Prevent duplicate injection on restarts
            }

            // 1. Create 10 Verified Student Sellers + 1 Admin
            User admin = new User();
            admin.setFullName("Campus Admin");
            admin.setEmail("admin@cput.ac.za");
            admin.setPassword(passwordEncoder.encode("admin1234"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            String[][] sellersData = {
                    {"Sipho Ndlovu", "sipho.ndlovu@mycput.ac.za", "0821234567", "District Six - Engineering Building"},
                    {"Amina Petersen", "amina.p@mycput.ac.za", "0719876543", "Bellville - Library Foyer"},
                    {"Tshepo Molefe", "tshepo.m@mycput.ac.za", "0835559812", "District Six - Student Centre"},
                    {"Chloe Van Der Merwe", "chloe.vdm@mycput.ac.za", "0641122334", "Granger Bay - Hotel School"},
                    {"Lwazi Mthembu", "lwazi.m@mycput.ac.za", "0784455667", "Bellville - IT Labs Block B"},
                    {"Fatima Hoosain", "fatima.h@mycput.ac.za", "0819988776", "Mowbray - Education Campus"},
                    {"Keagan Jacobs", "keagan.j@mycput.ac.za", "0736677889", "District Six - Commerce Building"},
                    {"Zandile Nkosi", "zandile.n@mycput.ac.za", "0843322110", "Wellington - Agri Block"},
                    {"Brandon Smith", "brandon.s@mycput.ac.za", "0725544332", "Bellville - Electrical Labs"},
                    {"Naledi Modise", "naledi.m@mycput.ac.za", "0829900112", "District Six - Design Building"}
            };

            List<User> sellers = new ArrayList<>();
            for (String[] s : sellersData) {
                User u = new User();
                u.setFullName(s[0]);
                u.setEmail(s[1]);
                u.setPassword(passwordEncoder.encode("password123"));
                u.setRole(Role.STUDENT);
                sellers.add(userRepository.save(u));
            }

            // 2. 50 Realistic Campus Listings across 6 Categories
            Object[][] listingsData = {
                    // Category: Textbooks & Notes (10 items)
                    {"Calculus: Early Transcendentals (8th Ed)", "James Stewart textbook, crisp pages with formula sheet attached. [Category: Textbooks & Notes | Condition: Like New | Delivery: Campus Meetup]", 480.00, 0, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=700&q=80"},
                    {"Introduction to Java Programming (11th Ed)", "Y. Daniel Liang, comprehensive edition for 2nd and 3rd year ICT students. [Category: Textbooks & Notes | Condition: Good Condition | Delivery: Campus Meetup, Courier Available]", 390.00, 1, "https://images.unsplash.com/photo-1532012164546-f432f2e3777f?auto=format&fit=crop&w=700&q=80"},
                    {"Database Systems: Design, Implementation & Management", "Coronel & Morris 13th edition. Clean pages, no highlights. [Category: Textbooks & Notes | Condition: Brand New | Delivery: Campus Meetup]", 520.00, 4, "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?auto=format&fit=crop&w=700&q=80"},
                    {"Principles of Economics (8th Ed) - N. Gregory Mankiw", "Essential for Business Management and Economics 101. [Category: Textbooks & Notes | Condition: Good Condition | Delivery: Campus Meetup]", 320.00, 6, "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?auto=format&fit=crop&w=700&q=80"},
                    {"Software Engineering (10th Ed) - Ian Sommerville", "Standard textbook for software development lifecycle modules. [Category: Textbooks & Notes | Condition: Like New | Delivery: Campus Meetup]", 410.00, 4, "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=700&q=80"},
                    {"Business Accounting 1 - Frank Wood", "Recommended for Accounting & Financial Management students. [Category: Textbooks & Notes | Condition: Fair / Usable | Delivery: Campus Meetup]", 220.00, 6, "https://images.unsplash.com/photo-1554415707-9e4c09d48d53?auto=format&fit=crop&w=700&q=80"},
                    {"Engineering Mechanics: Statics (14th Ed)", "Hibbeler statics guide with full worked problems booklet included. [Category: Textbooks & Notes | Condition: Like New | Delivery: Campus Meetup]", 450.00, 0, "https://images.unsplash.com/photo-1491841550275-ad7854e35ca6?auto=format&fit=crop&w=700&q=80"},
                    {"Financial Management for Non-Financial Managers", "Complete overview textbook with practice case solutions. [Category: Textbooks & Notes | Condition: Good Condition | Delivery: Campus Meetup]", 270.00, 6, "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?auto=format&fit=crop&w=700&q=80"},
                    {"Anatomy and Physiology: The Unity of Form and Function", "Saladin 8th Edition. High quality color medical diagrams. [Category: Textbooks & Notes | Condition: Good Condition | Delivery: Courier Available]", 600.00, 3, "https://images.unsplash.com/photo-1532938911079-1b06ac7ceec7?auto=format&fit=crop&w=700&q=80"},
                    {"Complete PRT300S Project Bound Study Notes & Past Exams", "Detailed printed notes and starred past test summaries for 3rd year. [Category: Textbooks & Notes | Condition: Brand New | Delivery: Campus Meetup]", 150.00, 4, "https://images.unsplash.com/photo-1455390582262-044cdead277a?auto=format&fit=crop&w=700&q=80"},

                    // Category: Electronics & Laptops (8 items)
                    {"Lenovo ThinkPad L14 (i5 10th Gen, 16GB RAM, 512GB SSD)", "Reliable coding laptop with original charger and 4-hour battery health. [Category: Electronics & Laptops | Condition: Like New | Delivery: Campus Meetup]", 4800.00, 4, "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?auto=format&fit=crop&w=700&q=80"},
                    {"Dell Latitude 5490 Laptop (Intel i5, 8GB, 256GB SSD)", "Good condition, clean Windows 11 installation, perfect for lectures. [Category: Electronics & Laptops | Condition: Good Condition | Delivery: Campus Meetup]", 3400.00, 8, "https://images.unsplash.com/photo-1541807084-5c52b6b3adef?auto=format&fit=crop&w=700&q=80"},
                    {"Samsung 24-inch Full HD IPS Borderless Monitor", "HDMI/VGA inputs, 75Hz refresh rate, includes HDMI power cord. [Category: Electronics & Laptops | Condition: Like New | Delivery: Campus Meetup]", 1350.00, 2, "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=700&q=80"},
                    {"Logitech MX Master 2S Wireless Ergonomic Mouse", "Rechargeable Bluetooth multi-device mouse with hyper-fast scroll. [Category: Electronics & Laptops | Condition: Good Condition | Delivery: Campus Meetup, Courier Available]", 550.00, 4, "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?auto=format&fit=crop&w=700&q=80"},
                    {"Redragon K552 Mechanical RGB Keyboard (Blue Switches)", "Compact 87-key tenkeyless mechanical board with braided USB. [Category: Electronics & Laptops | Condition: Like New | Delivery: Campus Meetup]", 450.00, 8, "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=700&q=80"},
                    {"Seagate 2TB External USB 3.0 Portable Hard Drive", "Pre-formatted exFAT, tested 100% health, ideal for project backups. [Category: Electronics & Laptops | Condition: Like New | Delivery: Campus Meetup]", 680.00, 2, "https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=700&q=80"},
                    {"Anker 20,000mAh PowerCore Fast-Charging Power Bank", "Dual USB-A and USB-C output, keeps phone charged during loadshedding. [Category: Electronics & Laptops | Condition: Brand New | Delivery: Campus Meetup]", 420.00, 1, "https://images.unsplash.com/photo-1609592426867-b50a36e9ff07?auto=format&fit=crop&w=700&q=80"},
                    {"Sony WH-CH520 Wireless Bluetooth Headphones", "35-hour battery life with lightweight on-ear cushions, black. [Category: Electronics & Laptops | Condition: Like New | Delivery: Campus Meetup, Courier Available]", 590.00, 3, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=700&q=80"},

                    // Category: Study Gear & Calculators (8 items)
                    {"Casio FX-991ZA Plus II Scientific Calculator", "CPUT exam approved, solar powered with sliding protective cover. [Category: Study Gear & Calculators | Condition: Like New | Delivery: Campus Meetup]", 320.00, 1, "https://images.unsplash.com/photo-1594980596870-8aa52a78d8cd?auto=format&fit=crop&w=700&q=80"},
                    {"Casio FX-82ZA Plus Natural Textbook Display", "Standard first-year engineering and finance scientific calculator. [Category: Study Gear & Calculators | Condition: Good Condition | Delivery: Campus Meetup]", 190.00, 0, "https://images.unsplash.com/photo-1587145820266-a5951ee6f620?auto=format&fit=crop&w=700&q=80"},
                    {"Rotring Engineering Technical Drawing Board (A3)", "Includes parallel motion ruler and angle adjusters for Civil/Mech. [Category: Study Gear & Calculators | Condition: Like New | Delivery: Campus Meetup]", 650.00, 0, "https://images.unsplash.com/photo-1581291518857-4e27b48ff24e?auto=format&fit=crop&w=700&q=80"},
                    {"Staedtler Mars 7-Piece Precision Technical Compass Set", "Includes extension bar and spare lead cases in a hardshell container. [Category: Study Gear & Calculators | Condition: Brand New | Delivery: Campus Meetup]", 240.00, 8, "https://images.unsplash.com/photo-1584697964190-7bb8c983a483?auto=format&fit=crop&w=700&q=80"},
                    {"LED Rechargeable Desk Lamp with Phone Stand", "3 color temperature modes, touch dimmer, rechargeable internal battery. [Category: Study Gear & Calculators | Condition: Brand New | Delivery: Campus Meetup]", 180.00, 5, "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=700&q=80"},
                    {"Faber-Castell 48 Classic Colour Pencil Tin", "High pigment art pencils for architecture and industrial design. [Category: Study Gear & Calculators | Condition: Brand New | Delivery: Campus Meetup]", 210.00, 9, "https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?auto=format&fit=crop&w=700&q=80"},
                    {"Helix Metric Scale Ruler 30cm (1:1 to 1:500)", "Triangular scale rule for technical drawings and quantity surveying. [Category: Study Gear & Calculators | Condition: Like New | Delivery: Campus Meetup]", 85.00, 0, "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=700&q=80"},
                    {"Ergonomic Laptop Stand (Aluminium Ventilated)", "Adjustable height to relieve neck strain during study sessions. [Category: Study Gear & Calculators | Condition: Brand New | Delivery: Campus Meetup, Courier Available]", 220.00, 2, "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=700&q=80"},

                    // Category: Uniforms & Lab Coats (8 items)
                    {"Official CPUT White Laboratory Coat (Size Medium)", "Pure cotton, button up front with 3 utility pockets. Clean condition. [Category: Uniforms & Lab Coats | Condition: Like New | Delivery: Campus Meetup]", 180.00, 3, "https://images.unsplash.com/photo-1584820927498-cfe5211fd8bf?auto=format&fit=crop&w=700&q=80"},
                    {"Chemical Splash Safety Goggles with Ventilation", "EN166 certified clear wrap-around eye protection for chemistry practicals. [Category: Uniforms & Lab Coats | Condition: Brand New | Delivery: Campus Meetup]", 75.00, 5, "https://images.unsplash.com/photo-1583947215259-38e31be8751f?auto=format&fit=crop&w=700&q=80"},
                    {"Nursing Scrubs Set - Navy Blue (Size Small)", "Top and trousers with elastic drawstring waistband. Soft fabric. [Category: Uniforms & Lab Coats | Condition: Good Condition | Delivery: Campus Meetup]", 260.00, 7, "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?auto=format&fit=crop&w=700&q=80"},
                    {"Chef Jacket & Checked Trousers Set (Size Large)", "Hospitality student uniform set, double breasted with cloth buttons. [Category: Uniforms & Lab Coats | Condition: Like New | Delivery: Campus Meetup]", 310.00, 3, "https://images.unsplash.com/photo-1577219491135-ce391730fb2c?auto=format&fit=crop&w=700&q=80"},
                    {"Safety Steel Toe-Cap Boots (Size 8 / 42)", "Required for workshop safety and civil construction site visits. [Category: Uniforms & Lab Coats | Condition: Good Condition | Delivery: Campus Meetup]", 350.00, 0, "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=700&q=80"},
                    {"High-Visibility Reflective Safety Vest (Lime)", "Class 2 safety vest with front zipper and clear ID badge pocket. [Category: Uniforms & Lab Coats | Condition: Brand New | Delivery: Campus Meetup]", 50.00, 8, "https://images.unsplash.com/photo-1607613009820-a29f7bb81c04?auto=format&fit=crop&w=700&q=80"},
                    {"Dissection Tool Kit in Leatherette Zipper Case", "10-piece stainless steel medical dissection set for biology. [Category: Uniforms & Lab Coats | Condition: Like New | Delivery: Campus Meetup]", 170.00, 5, "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=700&q=80"},
                    {"CPUT Branded Navy Track Jacket (Size Medium)", "Warm fleece lined official campus sports track top. [Category: Uniforms & Lab Coats | Condition: Good Condition | Delivery: Campus Meetup]", 240.00, 6, "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?auto=format&fit=crop&w=700&q=80"},

                    // Category: Campus Stalls & Services (8 items)
                    {"Assignment & Thesis Spiral Binding Service", "Same day plastic comb and wire binding with clear covers on campus. [Category: Campus Stalls & Services | Condition: Brand New | Delivery: Campus Meetup]", 35.00, 1, "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?auto=format&fit=crop&w=700&q=80"},
                    {"Student Haircut & Fade Session (Woodstock/D6)", "Professional clean tapers, fades, and beard trims near campus. [Category: Campus Stalls & Services | Condition: Brand New | Delivery: Campus Meetup]", 80.00, 2, "https://images.unsplash.com/photo-1503951914875-452162b0f3f1?auto=format&fit=crop&w=700&q=80"},
                    {"CV & LinkedIn Profile Optimization Service", "ATS-friendly tech resume template setup with mock STAR interview notes. [Category: Campus Stalls & Services | Condition: Brand New | Delivery: Campus Meetup]", 120.00, 4, "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d?auto=format&fit=crop&w=700&q=80"},
                    {"Handmade Gourmet Braai Sandwich / Toastie Stall", "Fresh toasted steak, cheese, and spicy polony sandwiches at lunchtime. [Category: Campus Stalls & Services | Condition: Brand New | Delivery: Campus Meetup]", 45.00, 3, "https://images.unsplash.com/photo-1528735602780-2552fd46c7af?auto=format&fit=crop&w=700&q=80"},
                    {"Laptop Dust Cleaning & Thermal Paste Service", "Reduce fan noise and thermal throttling with Arctic MX-4 paste. [Category: Campus Stalls & Services | Condition: Brand New | Delivery: Campus Meetup]", 150.00, 8, "https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?auto=format&fit=crop&w=700&q=80"},
                    {"Java & Python Programming 1-on-1 Tutoring", "Helps with arrays, OOP principles, SQL queries, and project debugging. [Category: Campus Stalls & Services | Condition: Brand New | Delivery: Campus Meetup]", 110.00, 4, "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=700&q=80"},
                    {"Handmade Beaded Graduation Stoles & Badges", "Custom South African beadwork accessories for graduation days. [Category: Campus Stalls & Services | Condition: Brand New | Delivery: Campus Meetup, Courier Available]", 160.00, 7, "https://images.unsplash.com/photo-1535223289827-42f1e9919769?auto=format&fit=crop&w=700&q=80"},
                    {"Photography Portrait Session for Grad / LinkedIn", "30-minute campus photo shoot with 5 edited high-res digital portraits. [Category: Campus Stalls & Services | Condition: Brand New | Delivery: Campus Meetup]", 250.00, 9, "https://images.unsplash.com/photo-1554080353-a576cf803bda?auto=format&fit=crop&w=700&q=80"},

                    // Category: General Essentials (8 items)
                    {"Karrimor 30L Waterproof Campus Backpack", "Padded laptop sleeve compartment, waterproof rain cover in base. [Category: General Essentials | Condition: Good Condition | Delivery: Campus Meetup]", 320.00, 2, "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=700&q=80"},
                    {"Russell Hobbs 1.7L Stainless Steel Cordless Kettle", "Auto shut-off, rapid boil heating element, clean descaled interior. [Category: General Essentials | Condition: Like New | Delivery: Campus Meetup]", 230.00, 5, "https://images.unsplash.com/photo-1594213114663-ddf4f240f08d?auto=format&fit=crop&w=700&q=80"},
                    {"Mini 2-Slice Sandwich Press & Grill", "Non-stick grill plates, compact size fits easily in residence rooms. [Category: General Essentials | Condition: Like New | Delivery: Campus Meetup]", 160.00, 2, "https://images.unsplash.com/photo-1583577612013-492f9e42cb70?auto=format&fit=crop&w=700&q=80"},
                    {"Foldable Residence Laundry Basket with Aluminium Frame", "Sturdy canvas hamper with handles for trips to campus washrooms. [Category: General Essentials | Condition: Brand New | Delivery: Campus Meetup]", 110.00, 7, "https://images.unsplash.com/photo-1582735689369-4fe89db7114c?auto=format&fit=crop&w=700&q=80"},
                    {"Hydro Flask Insulated 750ml Stainless Water Bottle", "Keeps cold water chilled for 24 hours during long lecture days. [Category: General Essentials | Condition: Like New | Delivery: Campus Meetup]", 175.00, 3, "https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=700&q=80"},
                    {"Yale Heavy Duty Padlock with 3 Spare Keys", "Solid brass casing, hardened steel shackle for hostel and gym lockers. [Category: General Essentials | Condition: Brand New | Delivery: Campus Meetup]", 95.00, 0, "https://images.unsplash.com/photo-1582139329536-e7284fece509?auto=format&fit=crop&w=700&q=80"},
                    {"Double Velvet Extra-Warm Winter Blanket", "Thick 2-ply soft plush blanket for cold winter residence nights. [Category: General Essentials | Condition: Like New | Delivery: Campus Meetup]", 340.00, 7, "https://images.unsplash.com/photo-1580301762395-21ce84d00bc6?auto=format&fit=crop&w=700&q=80"},
                    {"Schneider 4-Way Power Surge Protector Extension (3m)", "Prevents laptop and phone damage during loadshedding switches. [Category: General Essentials | Condition: Brand New | Delivery: Campus Meetup]", 130.00, 8, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=700&q=80"}
            };

            for (Object[] row : listingsData) {
                Listing item = new Listing();
                item.setTitle((String) row[0]);
                item.setDescription((String) row[1]);
                item.setPrice(BigDecimal.valueOf((Double) row[2]));

                int sellerIndex = (Integer) row[3];
                item.setSeller(sellers.get(sellerIndex));
                item.setLocation(sellersData[sellerIndex][3]);
                item.setContactInfo(sellersData[sellerIndex][2]);
                item.setStatus(ListingStatus.APPROVED); // Immediately active on buyer feed
                item.setImageUrls(List.of((String) row[4]));
                listingRepository.save(item);
            }

            System.out.println(">>> SEED SUCCESS: 10 Verified Sellers and 50 Active Campus Products Initialized.");
        };
    }
}