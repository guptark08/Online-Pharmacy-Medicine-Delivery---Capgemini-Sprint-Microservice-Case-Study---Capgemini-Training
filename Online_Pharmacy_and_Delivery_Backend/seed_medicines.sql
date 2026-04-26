-- Idempotent seed script for pharmacy_catalog_db.
-- Safe for repeated runs: inserts only missing categories and medicines.

USE pharmacy_catalog_db;

START TRANSACTION;

INSERT INTO categories (name, description, is_active)
SELECT 'Pain Relief', 'Medicines for pain and fever', b'1'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE LOWER(name) = LOWER('Pain Relief')
);

INSERT INTO categories (name, description, is_active)
SELECT 'Antibiotics', 'Medicines for bacterial infections', b'1'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE LOWER(name) = LOWER('Antibiotics')
);

INSERT INTO categories (name, description, is_active)
SELECT 'Cold & Cough', 'Medicines for cold, cough, and allergies', b'1'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE LOWER(name) = LOWER('Cold & Cough')
);

INSERT INTO categories (name, description, is_active)
SELECT 'Diabetes', 'Medicines for managing blood sugar', b'1'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE LOWER(name) = LOWER('Diabetes')
);

INSERT INTO categories (name, description, is_active)
SELECT 'Cardiac', 'Medicines for heart and blood pressure', b'1'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE LOWER(name) = LOWER('Cardiac')
);

INSERT INTO categories (name, description, is_active)
SELECT 'Digestion & Antacids', 'Medicines for stomach issues', b'1'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE LOWER(name) = LOWER('Digestion & Antacids')
);

INSERT INTO categories (name, description, is_active)
SELECT 'Vitamins & Supplements', 'Nutritional supplements', b'1'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE LOWER(name) = LOWER('Vitamins & Supplements')
);

INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Dolo 650 Tablet', 'Micro Labs Ltd', 'Paracetamol tablet used for pain relief and fever', 30.50, 28.00, 500, b'0', '1 tablet SOS', 'Nausea, rash', NULL, '2027-12-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Pain Relief') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Dolo 650 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Crocin Advance', 'GSK', 'Fast absorbing paracetamol for quick relief', 15.00, 14.50, 600, b'0', '1 tablet 3-4 times a day', 'Stomach pain', NULL, '2027-10-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Pain Relief') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Crocin Advance'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Calpol 500mg Tablet', 'GSK', 'Mild analgesic and antipyretic', 14.20, 14.00, 450, b'0', '1 tablet SOS', 'Rare', NULL, '2027-05-30', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Pain Relief') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Calpol 500mg Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Combiflam Tablet', 'Sanofi India', 'Ibuprofen + Paracetamol for pain and inflammation', 45.00, 42.00, 300, b'1', '1 tablet twice a day', 'Acidity, heartburn', NULL, '2027-08-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Pain Relief') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Combiflam Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Saridon Tablet', 'Piramal', 'Effective against severe headache', 35.00, 34.00, 400, b'0', '1 tablet for headache', 'Dizziness', NULL, '2027-11-30', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Pain Relief') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Saridon Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Voveran Plus Tablet', 'Novartis', 'Diclofenac + Paracetamol', 95.00, 85.00, 200, b'1', '1 tablet twice a day', 'Nausea, acidity', NULL, '2027-12-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Pain Relief') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Voveran Plus Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Zerodol-SP Tablet', 'Ipca Labs', 'Aceclofenac + Paracetamol + Serratiopeptidase', 120.00, 110.00, 150, b'1', '1 tablet twice daily', 'Stomach upset', NULL, '2027-06-30', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Pain Relief') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Zerodol-SP Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Zifi 200 Tablet', 'FDC Ltd', 'Cefixime antibiotic', 110.00, 100.00, 200, b'1', '1 tablet twice daily', 'Diarrhea', NULL, '2027-03-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Antibiotics') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Zifi 200 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Augmentin 625 Duo Tablet', 'GSK', 'Amoxicillin + Clavulanic Acid', 200.00, 185.00, 300, b'1', '1 tablet twice daily', 'Nausea, Diarrhea', NULL, '2027-11-30', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Antibiotics') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Augmentin 625 Duo Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Azithral 500 Tablet', 'Alembic', 'Azithromycin for throat/lung infections', 120.00, 115.00, 400, b'1', '1 tablet daily for 3-5 days', 'Stomach upset', NULL, '2027-04-30', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Antibiotics') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Azithral 500 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Taxim-O 200 Tablet', 'Alkem Labs', 'Cefixime antibiotic', 130.00, 120.00, 150, b'1', '1 tablet twice daily', 'Diarrhea', NULL, '2027-02-28', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Antibiotics') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Taxim-O 200 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Monocef-O 200 Tablet', 'Aristo Pharma', 'Cefpodoxime antibiotic', 150.00, 140.00, 180, b'1', '1 tablet twice daily', 'Rash, nausea', NULL, '2027-09-30', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Antibiotics') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Monocef-O 200 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Roxid 150 Tablet', 'Alembic', 'Roxithromycin', 140.00, 130.00, 150, b'1', '1 tablet twice daily', 'Stomach upset', NULL, '2027-10-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Antibiotics') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Roxid 150 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Novamox 500 Capsule', 'Cipla', 'Amoxicillin', 100.00, 95.00, 200, b'1', '1 capsule thrice a day', 'Diarrhea', NULL, '2027-08-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Antibiotics') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Novamox 500 Capsule'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Cheston Cold Tablet', 'Cipla', 'Cetirizine + Paracetamol + Phenylephrine', 45.00, 40.00, 500, b'0', '1 tablet twice a day', 'Drowsiness', NULL, '2027-07-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Cold & Cough') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Cheston Cold Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Sinarest Tablet', 'Centaur', 'Paracetamol + Chlorpheniramine + Phenylephrine', 55.00, 50.00, 600, b'0', '1 tablet twice a day', 'Sleepiness', NULL, '2027-09-30', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Cold & Cough') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Sinarest Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Honitus Cough Syrup', 'Dabur', 'Ayurvedic cough syrup', 95.00, 90.00, 300, b'0', '2 teaspoons twice a day', 'None', NULL, '2027-12-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Cold & Cough') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Honitus Cough Syrup'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Benadryl Cough Syrup', 'J&J', 'Diphenhydramine', 120.00, 110.00, 400, b'0', '2 teaspoons 3 times a day', 'Drowsiness', NULL, '2027-05-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Cold & Cough') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Benadryl Cough Syrup'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Alex Syrup', 'Glenmark', 'Dextromethorphan + Chlorpheniramine', 115.00, 105.00, 250, b'0', '1 teaspoon twice a day', 'Dizziness', NULL, '2027-11-30', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Cold & Cough') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Alex Syrup'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Ascoril LS Syrup', 'Glenmark', 'Levosalbutamol + Ambroxol + Guaifenesin', 118.00, 110.00, 300, b'1', '1 teaspoon twice a day', 'Tremors', NULL, '2027-08-28', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Cold & Cough') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Ascoril LS Syrup'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Glycomet 500 Tablet', 'USV Ltd', 'Metformin for Type 2 Diabetes', 65.00, 60.00, 800, b'1', '1 tablet after meal', 'Nausea, stomach upset', NULL, '2027-12-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Diabetes') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Glycomet 500 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Jalra M 50/500 Tablet', 'USV Ltd', 'Vildagliptin + Metformin', 250.00, 230.00, 200, b'1', '1 tablet twice a day', 'Hypoglycemia', NULL, '2027-10-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Diabetes') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Jalra M 50/500 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Galvus Met 50/500 Tablet', 'Novartis', 'Vildagliptin + Metformin', 300.00, 280.00, 150, b'1', '1 tablet twice a day', 'Tremors', NULL, '2027-09-30', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Diabetes') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Galvus Met 50/500 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Janumet 50/500 Tablet', 'MSD', 'Sitagliptin + Metformin', 350.00, 330.00, 180, b'1', '1 tablet twice a day', 'Hypoglycemia', NULL, '2027-08-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Diabetes') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Janumet 50/500 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Amaryl M 1mg Tablet', 'Sanofi', 'Glimepiride + Metformin', 150.00, 140.00, 400, b'1', '1 tablet daily before breakfast', 'Weight gain', NULL, '2027-07-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Diabetes') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Amaryl M 1mg Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Gluconorm-G 1 Tablet', 'Lupin', 'Glimepiride + Metformin', 160.00, 150.00, 350, b'1', '1 tablet daily', 'Low blood sugar', NULL, '2027-11-30', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Diabetes') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Gluconorm-G 1 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Telma 40 Tablet', 'Glenmark', 'Telmisartan for hypertension', 120.00, 110.00, 500, b'1', '1 tablet daily', 'Dizziness', NULL, '2027-12-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Cardiac') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Telma 40 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Amlokind 5 Tablet', 'Mankind Pharma', 'Amlodipine', 45.00, 40.00, 600, b'1', '1 tablet daily', 'Swelling in ankles', NULL, '2027-07-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Cardiac') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Amlokind 5 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Stamlo 5 Tablet', 'Dr. Reddy', 'Amlodipine', 60.00, 55.00, 400, b'1', '1 tablet daily', 'Dizziness', NULL, '2027-12-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Cardiac') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Stamlo 5 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Concor 5 Tablet', 'Merck', 'Bisoprolol', 95.00, 90.00, 300, b'1', '1 tablet daily', 'Fatigue', NULL, '2027-09-30', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Cardiac') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Concor 5 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Rosumac 10 Tablet', 'Macleods', 'Rosuvastatin', 180.00, 170.00, 250, b'1', '1 tablet at night', 'Muscle pain', NULL, '2027-10-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Cardiac') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Rosumac 10 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Atorva 10 Tablet', 'Zydus', 'Atorvastatin', 120.00, 110.00, 300, b'1', '1 tablet at night', 'Joint pain', NULL, '2027-10-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Cardiac') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Atorva 10 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Ecosprin 75 Tablet', 'USV Ltd', 'Aspirin blood thinner', 15.00, 14.00, 1000, b'1', '1 tablet daily', 'Bleeding tendency', NULL, '2027-11-30', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Cardiac') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Ecosprin 75 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Clopilet 75 Tablet', 'Sun Pharma', 'Clopidogrel', 80.00, 75.00, 500, b'1', '1 tablet daily', 'Bleeding', NULL, '2027-05-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Cardiac') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Clopilet 75 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Pan 40 Tablet', 'Alkem Labs', 'Pantoprazole for acidity', 140.00, 130.00, 800, b'0', '1 tablet empty stomach', 'Headache', NULL, '2027-12-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Digestion & Antacids') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Pan 40 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Pantocid 40 Tablet', 'Sun Pharma', 'Pantoprazole', 150.00, 140.00, 600, b'0', '1 tablet empty stomach', 'Dizziness', NULL, '2027-08-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Digestion & Antacids') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Pantocid 40 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Rablet 20 Tablet', 'Lupin', 'Rabeprazole', 130.00, 120.00, 400, b'0', '1 tablet empty stomach', 'Nausea', NULL, '2027-11-30', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Digestion & Antacids') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Rablet 20 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Omez 20 Capsule', 'Dr. Reddy', 'Omeprazole', 65.00, 60.00, 700, b'0', '1 capsule empty stomach', 'Stomach pain', NULL, '2027-06-30', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Digestion & Antacids') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Omez 20 Capsule'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Digene Antacid Tablet', 'Abbott', 'Antacid for quick relief', 25.00, 24.00, 500, b'0', 'Chew 1-2 tablets as required', 'Constipation', NULL, '2027-10-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Digestion & Antacids') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Digene Antacid Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Gelusil MPS Liquid', 'Pfizer', 'Liquid antacid', 120.00, 110.00, 300, b'0', '2 teaspoons after meals', 'Diarrhea', NULL, '2027-12-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Digestion & Antacids') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Gelusil MPS Liquid'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Aciloc 150 Tablet', 'Cadila', 'Ranitidine', 35.00, 32.00, 600, b'0', '1 tablet twice a day', 'Headache', NULL, '2027-10-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Digestion & Antacids') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Aciloc 150 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Metrogyl 400 Tablet', 'J.B. Chemicals', 'Metronidazole', 25.00, 23.00, 450, b'1', '1 tablet thrice a day', 'Metallic taste', NULL, '2027-09-30', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Digestion & Antacids') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Metrogyl 400 Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Sporlac DS Tablet', 'Sanzyme', 'Lactobacillus probiotic', 65.00, 60.00, 400, b'0', '1 tablet twice a day', 'Gas', NULL, '2027-12-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Digestion & Antacids') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Sporlac DS Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Enterogermina Capsule', 'Sanofi', 'Probiotic', 250.00, 240.00, 200, b'0', '1 vial daily', 'None', NULL, '2027-08-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Digestion & Antacids') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Enterogermina Capsule'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Vizylac Capsule', 'Unichem', 'Lactic Acid Bacillus', 55.00, 50.00, 500, b'0', '1 capsule daily', 'None', NULL, '2027-09-30', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Digestion & Antacids') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Vizylac Capsule'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Becosules Capsule', 'Pfizer', 'B-Complex vitamin', 45.00, 42.00, 800, b'0', '1 capsule daily', 'Yellow urine', NULL, '2027-12-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Vitamins & Supplements') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Becosules Capsule'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Supradyn Daily Tablet', 'Bayer', 'Multivitamin supplement', 55.00, 50.00, 600, b'0', '1 tablet daily', 'Nausea', NULL, '2027-10-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Vitamins & Supplements') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Supradyn Daily Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Zincovit Tablet', 'Apex Labs', 'Multivitamin with Zinc', 105.00, 95.00, 700, b'0', '1 tablet daily', 'Constipation', NULL, '2027-05-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Vitamins & Supplements') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Zincovit Tablet'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Evion 400 Capsule', 'Merck', 'Vitamin E supplement', 35.00, 32.00, 900, b'0', '1 capsule daily', 'None', NULL, '2027-11-30', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Vitamins & Supplements') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Evion 400 Capsule'));
INSERT INTO medicines (name, manufacturer, description, price, discounted_price, stock, requires_prescription, dosage_info, side_effects, image_url, expiry_date, status, is_active, category_id, created_at, updated_at)
SELECT 'Shelcal 500 Tablet', 'Torrent', 'Calcium + Vitamin D3', 110.00, 100.00, 500, b'0', '1 tablet daily', 'Constipation', NULL, '2027-07-31', 'AVAILABLE', b'1', (SELECT id FROM categories WHERE LOWER(name) = LOWER('Vitamins & Supplements') LIMIT 1), NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE LOWER(name) = LOWER('Shelcal 500 Tablet'));

COMMIT;
