# Guide de captures — TP 03 `cruddemo`

Ce guide décrit, pas à pas, comment reproduire et photographier les résultats du TP sur
**Xubuntu** avec **VS Code**. Les sorties attendues sont les sorties réelles observées sur ce
projet ; elles sont aussi archivées dans `evidence/`.

> Toutes les commandes se lancent depuis la racine du projet :
>
> ```bash
> cd ~/Documents/s5/spring/tp3/cruddemo
> ```

---

## 0. Préparation de l'environnement

### 0.1 Vérifier la base

```bash
mysql -u springstudent -pspringstudent student_tracker -e "SELECT COUNT(*) FROM student;"
```

Sortie attendue : un nombre (0 au départ). Si la commande échoue, vérifier que MySQL est démarré
et que la base `student_tracker` existe (script : `src/main/resources/sql/02-student-tracker.sql`).

### 0.2 Commandes de remise à zéro

À utiliser avant chaque scénario pour repartir d'une table vide et d'identifiants propres :

```bash
mysql -u springstudent -pspringstudent student_tracker \
  -e "DELETE FROM student; ALTER TABLE student AUTO_INCREMENT = 1;"
```

Vérification (format tableau lisible pour les captures) :

```bash
mysql -t -u springstudent -pspringstudent student_tracker \
  -e "SELECT * FROM student;" 2>/dev/null
```

Sortie attendue :

```text
+----+------------+-----------+-------+
| id | first_name | last_name | email |
+----+------------+-----------+-------+
+----+------------+-----------+-------+
```

### 0.3 Agrandir le texte et préparer les captures

**Terminal Xubuntu (xfce4-terminal)**

- Menu `Édition` → `Préférences` → `Apparence` → police `Monospace 14` ou `16`.
- Ou raccourci `Ctrl` + `+` pour agrandir.
- Pour une session ponctuelle :

  ```bash
  xfce4-terminal --font="Monospace 16"
  ```

**Terminal intégré VS Code**

- `Ctrl` + `=` pour agrandir.
- Ou `Fichier` → `Préférences` → `Paramètres` → `terminal.integrated.fontSize` = `16`.

**Captures d'écran sous Xubuntu**

Le dossier `captures/` existe déjà (il contient `.gitkeep`). Depuis la racine du projet :

```bash
# Fenêtre active (la plus pratique) :
xfce4-screenshooter -w -s captures/01-create.png

# Plein écran :
xfce4-screenshooter -f -s captures/01-create.png

# Sélection d'une zone :
xfce4-screenshooter -r -s captures/01-create.png

# Avec 3 secondes de délai (pour placer la fenêtre) :
xfce4-screenshooter -w -d 3 -s captures/01-create.png
```

Raccourcis XFCE par défaut :

- `Impr. écran` (`PrtSc`) : dialogue de capture ;
- `Alt` + `Impr. écran` : fenêtre active ;
- `Maj` + `Impr. écran` : sélection d'une zone.

Selon la configuration, le dialogue enregistre dans `~/Pictures`. Déplacer ensuite le fichier :

```bash
mv ~/Pictures/capture.png captures/01-create.png
```

**Conseils de qualité**

- Maximiser la fenêtre du terminal et garder la commande visible avec sa sortie.
- Éviter de couper les lignes : agrandir la fenêtre ou réduire la police si nécessaire.
- Ne pas utiliser `clear` après la commande.
- Vérifier le fichier créé avec `ls -l captures/` ou en l'ouvrant dans VS Code.

### 0.4 Astuce : filtrer les logs SQL

L'application est configurée avec :

```properties
logging.level.root=warn
logging.level.org.hibernate.SQL=debug
logging.level.org.hibernate.orm.jdbc.bind=trace
```

Pour une capture lisible contenant uniquement l'essentiel :

```bash
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=create 2>&1 \
  | grep -iE "insert|binding|Saved"
```

Pour conserver une trace complète en parallèle :

```bash
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=create 2>&1 \
  | tee evidence/create.log
```

`-q` masque les logs Maven ; les logs Hibernate et les `System.out.println` restent visibles.
L'application se termine seule après l'action (ce n'est pas un serveur web).

---

## 1. Scénario requis, dans l'ordre

L'ordre ci-dessous est cohérent : on part d'une table vide, on crée, on lit, on modifie, puis on
supprime. Les identifiants attendus sont 1, 2, 3 après une remise à zéro.

### CAP-01 — Console de `create` (identifiants générés)

**Remise à zéro :**

```bash
mysql -u springstudent -pspringstudent student_tracker \
  -e "DELETE FROM student; ALTER TABLE student AUTO_INCREMENT = 1;"
```

**Commande à lancer :**

```bash
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=create 2>&1 \
  | grep -E "Saved student|insert into|binding parameter"
```

**Nom de fichier suggéré :** `captures/01-create.png`

**Sortie attendue (réelle) :**

```text
insert into student (email,first_name,last_name) values (?,?,?)
binding parameter (1:VARCHAR) <- [paul@test.com]
binding parameter (2:VARCHAR) <- [Paul]
binding parameter (3:VARCHAR) <- [Doe]
Saved student. Generated id: 1
insert into student (email,first_name,last_name) values (?,?,?)
binding parameter (1:VARCHAR) <- [mary@test.com]
binding parameter (2:VARCHAR) <- [Mary]
binding parameter (3:VARCHAR) <- [Public]
Saved student. Generated id: 2
insert into student (email,first_name,last_name) values (?,?,?)
binding parameter (1:VARCHAR) <- [susan@test.com]
binding parameter (2:VARCHAR) <- [Susan]
binding parameter (3:VARCHAR) <- [Doe]
Saved student. Generated id: 3
```

**Doit être visible :** la commande, les **trois** lignes `Saved student. Generated id: 1/2/3`,
et si possible les `insert into student` avec les paramètres liés.

**État de la base après :** 3 lignes.

---

### CAP-02 — `SELECT` après `create`

**Commande :**

```bash
mysql -t -u springstudent -pspringstudent student_tracker \
  -e "SELECT id, first_name, last_name, email FROM student ORDER BY id;" 2>/dev/null
```

**Nom de fichier suggéré :** `captures/02-select-apres-create.png`

**Sortie attendue :**

```text
+----+------------+-----------+----------------+
| id | first_name | last_name | email          |
+----+------------+-----------+----------------+
|  1 | Paul       | Doe       | paul@test.com  |
|  2 | Mary       | Public    | mary@test.com  |
|  3 | Susan      | Doe       | susan@test.com |
+----+------------+-----------+----------------+
```

**Doit être visible :** les 3 lignes et les 3 identifiants. C'est la preuve que `persist` a bien
inséré les étudiants.

---

### CAP-03 — Console de `read`

**Commande :**

```bash
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=read 2>&1 \
  | grep -E "First student|All students|Students with lastName|Student\\{|select|binding parameter"
```

**Nom de fichier suggéré :** `captures/03-read.png`

**Sortie attendue (réelle) :**

```text
First student (id 1) via findById: Student{id=1, firstName='Paul', lastName='Doe', email='paul@test.com'}
All students (ORDER BY lastName):
Student{id=1, firstName='Paul', lastName='Doe', email='paul@test.com'}
Student{id=3, firstName='Susan', lastName='Doe', email='susan@test.com'}
Student{id=2, firstName='Mary', lastName='Public', email='mary@test.com'}
Students with lastName 'Doe':
Student{id=1, firstName='Paul', lastName='Doe', email='paul@test.com'}
Student{id=3, firstName='Susan', lastName='Doe', email='susan@test.com'}
```

**Doit être visible :** le premier étudiant relu par `findById`, la liste triée par `lastName`,
puis le résultat de `findByLastName("Doe")`. Le premier étudiant est pris dynamiquement dans
`findAll()`, pas codé en dur.

---

### CAP-04 — `SELECT` avant la modification

**Commande :**

```bash
mysql -t -u springstudent -pspringstudent student_tracker \
  -e "SELECT id, first_name, last_name, email FROM student ORDER BY id;" 2>/dev/null
```

**Nom de fichier suggéré :** `captures/04-select-avant-update.png`

**Sortie attendue :** la même que CAP-02, avec `Paul` en `first_name` pour l'id 1.

**Doit être visible :** l'état **avant** modification. Cette capture se place avant CAP-05.

---

### CAP-05 — Console de `update`

**Commande :**

```bash
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=update 2>&1 \
  | grep -E "Updated student|update student|binding parameter"
```

**Nom de fichier suggéré :** `captures/05-update.png`

**Sortie attendue (réelle) :**

```text
update student set email=?,first_name=?,last_name=? where id=?
binding parameter (1:VARCHAR) <- [paul@test.com]
binding parameter (2:VARCHAR) <- [John]
binding parameter (3:VARCHAR) <- [Doe]
binding parameter (4:INTEGER) <- [1]
Updated student: Student{id=1, firstName='John', lastName='Doe', email='paul@test.com'}
```

**Doit être visible :** l'`UPDATE` SQL (ou au minimum la ligne `Updated student:`) et le prénom
`John` pour l'id 1. L'action prend le premier étudiant renvoyé par `findAll()` (tri par
`lastName`), le passe à `John`, appelle `merge`, puis relit avec `findById`.

---

### CAP-06 — `SELECT` après la modification

**Commande :**

```bash
mysql -t -u springstudent -pspringstudent student_tracker \
  -e "SELECT id, first_name, last_name, email FROM student ORDER BY id;" 2>/dev/null
```

**Nom de fichier suggéré :** `captures/06-select-apres-update.png`

**Sortie attendue :**

```text
+----+------------+-----------+----------------+
| id | first_name | last_name | email          |
+----+------------+-----------+----------------+
|  1 | John       | Doe       | paul@test.com  |
|  2 | Mary       | Public    | mary@test.com  |
|  3 | Susan      | Doe       | susan@test.com |
+----+------------+-----------+----------------+
```

**Doit être visible :** `John` pour l'id 1, les autres lignes inchangées. CAP-04 + CAP-06
constituent la preuve « avant / après modification ».

---

### CAP-07 — Console de `delete`

**Commande :**

```bash
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=delete 2>&1 \
  | grep -E "Deleted student|After delete|delete\\(9999\\)|delete from student|binding parameter"
```

**Nom de fichier suggéré :** `captures/07-delete.png`

**Sortie attendue (réelle) :**

```text
delete from student where id=?
binding parameter (1:INTEGER) <- [1]
Deleted student with id 1: true
After delete, findById(1) = null
delete(9999) -> false (no student with this id, nothing deleted)
```

**Doit être visible :** la suppression de l'id 1, `findById(1) = null`, et le cas null-safe
`delete(9999) -> false` qui ne lève pas de `NullPointerException`.

---

### CAP-08 — `SELECT` après `delete`

**Commande :**

```bash
mysql -t -u springstudent -pspringstudent student_tracker \
  -e "SELECT id, first_name, last_name, email FROM student ORDER BY id;" 2>/dev/null
```

**Nom de fichier suggéré :** `captures/08-select-apres-delete.png`

**Sortie attendue :**

```text
+----+------------+-----------+----------------+
| id | first_name | last_name | email          |
+----+------------+-----------+----------------+
|  2 | Mary       | Public    | mary@test.com  |
|  3 | Susan      | Doe       | susan@test.com |
+----+------------+-----------+----------------+
```

**Doit être visible :** l'id 1 a disparu ; le trou dans la numérotation est normal
(`AUTO_INCREMENT` ne réutilise pas les identifiants supprimés).

---

### CAP-09 — Extrait des logs SQL : `INSERT` + valeurs liées

C'est la capture demandée pour prouver que Hibernate utilise des **paramètres liés** et non de la
concaténation.

**Remise à zéro :**

```bash
mysql -u springstudent -pspringstudent student_tracker \
  -e "DELETE FROM student; ALTER TABLE student AUTO_INCREMENT = 1;"
```

**Commande :**

```bash
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=create 2>&1 \
  | grep -iE "insert|binding|Saved"
```

**Nom de fichier suggéré :** `captures/09-logs-sql-insert.png`

**Sortie attendue :** voir CAP-01. On doit voir au minimum :

```text
insert into student (email,first_name,last_name) values (?,?,?)
binding parameter (1:VARCHAR) <- [paul@test.com]
binding parameter (2:VARCHAR) <- [Paul]
binding parameter (3:VARCHAR) <- [Doe]
Saved student. Generated id: 1
```

**Doit être visible :** le mot-clé `insert into student`, les `?` (requête préparée) et les lignes
`binding parameter (...) <- [...]`.

**Remarque :** la table contient de nouveau 3 lignes à la fin de cette capture. C'est le point de
départ des captures optionnelles ci-dessous. Si vous voulez revenir à l'état vide, relancez la
commande de remise à zéro du §0.2.

---

## 2. Captures optionnelles (bonus et démonstrations)

À partir de CAP-09, la table contient les ids 1, 2, 3. Enchaîner les commandes ci-dessous dans
cet ordre.

### CAP-10 — `findall` (tri ascendant et descendant)

```bash
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=findall 2>&1 \
  | grep -E "All students|Student\\{"
```

**Nom suggéré :** `captures/10-findall.png`

**Sortie attendue :**

```text
All students (ORDER BY lastName ASC):
Student{id=1, firstName='Paul', lastName='Doe', email='paul@test.com'}
Student{id=3, firstName='Susan', lastName='Doe', email='susan@test.com'}
Student{id=2, firstName='Mary', lastName='Public', email='mary@test.com'}
All students (ORDER BY lastName DESC):
Student{id=2, firstName='Mary', lastName='Public', email='mary@test.com'}
Student{id=1, firstName='Paul', lastName='Doe', email='paul@test.com'}
Student{id=3, firstName='Susan', lastName='Doe', email='susan@test.com'}
```

### CAP-11 — `bylastname`

```bash
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=bylastname 2>&1 \
  | grep -E "Students with lastName|Student\\{|binding parameter"
```

**Nom suggéré :** `captures/11-bylastname.png`

Sortie attendue : `Students with lastName 'Doe':` puis Paul et Susan, avec
`binding parameter (1:VARCHAR) <- [Doe]`.

### CAP-12 — `bulkupdate`

```bash
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=bulkupdate 2>&1 \
  | grep -E "Rows updated|update student|binding parameter"
```

**Nom suggéré :** `captures/12-bulkupdate.png`

**Sortie attendue :**

```text
update student s1_0 set email=? where s1_0.last_name=?
binding parameter (1:VARCHAR) <- [unknown@test.com]
binding parameter (2:VARCHAR) <- [Doe]
Rows updated by bulk update: 2
```

Vérification SQL (facultative, capture séparée `12b`) :

```bash
mysql -t -u springstudent -pspringstudent student_tracker \
  -e "SELECT id, first_name, last_name, email FROM student ORDER BY id;" 2>/dev/null
```

Les emails de Paul (`id=1`) et de Susan (`id=3`) deviennent `unknown@test.com`, car les deux
ont `last_name='Doe'`. C'est le comportement normal d'un `UPDATE` bulk : il modifie **toutes**
les lignes qui correspondent au critère.

> Remarque : si l'action `update` a déjà été exécutée avant (Paul devient John), le critère
> `lastName = 'Doe'` correspond toujours à Paul/John et à Susan ; le nombre de lignes reste 2.
> Dans l'ancien essai unitaire de `evidence/delete.log`, l'id 1 avait été supprimé avant le
> bulkupdate, d'où un compte de 1 à ce moment-là.

### CAP-13 — `named` (`@NamedQuery`)

```bash
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=named 2>&1 \
  | grep -E "named query|Student\\{|binding parameter"
```

**Nom suggéré :** `captures/13-named.png`

Sortie attendue : `Students with lastName 'Doe' (named query):` puis Paul et Susan. Le SQL et
les paramètres liés sont identiques à `bylastname` ; seule la déclaration de la requête change.

### CAP-14 — `service` (couche service + transaction)

```bash
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=service 2>&1 \
  | grep -E "via service|insert into|update student|binding parameter"
```

**Nom suggéré :** `captures/14-service.png`

**Sortie attendue (si les ids 1 à 3 existent déjà) :**

```text
Created via service: Student{id=4, firstName='Alice', lastName='Service', email='alice@service.com'}
Created via service: Student{id=5, firstName='Bobby', lastName='Service', email='bob@service.com'}
Updated via service: Student{id=5, firstName='Bobby', lastName='Service', email='bob@service.com'}
```

Le service crée Alice et Bob, puis renomme Bob en Bobby, le tout dans une seule transaction
de service (`createTwoAndUpdateFirstName`).

### CAP-15 — `deleteall`

```bash
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=deleteall 2>&1 \
  | grep -E "Deleted row count|delete"
```

**Nom suggéré :** `captures/15-deleteall.png`

Si vous avez suivi l'ordre CAP-09 → CAP-14, la table contient 5 lignes (Paul, Mary, Susan, Alice,
Bobby) : la sortie attendue est donc :

```text
Deleted row count: 5
```

> Dans `evidence/deleteall.log`, la valeur observée est `4` car le scénario archivé avait
> d'abord exécuté `delete` (suppression de l'id 1) avant `service` et `deleteall`. Les deux
> valeurs sont correctes ; elles dépendent du nombre de lignes présentes au moment de l'appel.

Vérification :

```bash
mysql -t -u springstudent -pspringstudent student_tracker \
  -e "SELECT COUNT(*) AS total FROM student;" 2>/dev/null
```

CAP-16 (facultative) : `captures/16-select-apres-deleteall.png`, avec `total = 0`.

### CAP-17 — Action inconnue

```bash
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=inconnu 2>&1 \
  | grep -E "Action inconnue"
```

**Nom suggéré :** `captures/17-action-inconnue.png`

Sortie attendue :

```text
Action inconnue: inconnu
```

---

## 3. Capture du jar exécutable

```bash
./mvnw -q package -DskipTests
ls -1 target/*.jar
```

Le nom réel observé est :

```text
target/cruddemo-0.0.1-SNAPSHOT.jar
```

Scénario facultatif :

```bash
mysql -u springstudent -pspringstudent student_tracker \
  -e "DELETE FROM student; ALTER TABLE student AUTO_INCREMENT = 1;"

java -jar target/cruddemo-0.0.1-SNAPSHOT.jar create
java -jar target/cruddemo-0.0.1-SNAPSHOT.jar read
java -jar target/cruddemo-0.0.1-SNAPSHOT.jar update
java -jar target/cruddemo-0.0.1-SNAPSHOT.jar delete

mysql -t -u springstudent -pspringstudent student_tracker \
  -e "SELECT * FROM student;" 2>/dev/null
```

**Nom suggéré :** `captures/18-jar-actions.png`

La sortie complète est archivée dans `evidence/jar-actions.log`. Le jar se comporte exactement
comme `spring-boot:run` : il lit `args[0]` et s'arrête à la fin de l'action.

---

## 4. Capture du bonus 1 (génération de schéma)

Le plus simple est d'ouvrir le compte rendu déjà produit :

```bash
cat docs/bonus1-create-table.txt
```

**Nom suggéré :** `captures/19-bonus1-ddl.png`

On doit y voir les deux lignes DDL :

```text
drop table if exists student
create table student (id integer not null auto_increment, email varchar(255),
    first_name varchar(255), last_name varchar(255), primary key (id)) engine=InnoDB
```

Si l'enseignant exige de reproduire le log en direct, voici la procédure **complète** (attention :
elle supprime la table) :

```bash
# 1) Sauvegarde
mysqldump -u springstudent -pspringstudent student_tracker student \
  > evidence/bonus1-backup.sql

# 2) Suppression de la table
mysql -u springstudent -pspringstudent student_tracker -e "DROP TABLE student;"

# 3) Dans src/main/resources/application.properties, mettre :
#    spring.jpa.hibernate.ddl-auto=create
#    puis lancer :
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=read 2>&1 \
  | grep -iE "drop table|create table"

# 4) Vérifier :
mysql -t -u springstudent -pspringstudent student_tracker \
  -e "SHOW CREATE TABLE student\G" 2>/dev/null

# 5) RESTAURER OBLIGATOIREMENT :
#    spring.jpa.hibernate.ddl-auto=none
mysql -u springstudent -pspringstudent student_tracker -e "
DROP TABLE IF EXISTS student;
CREATE TABLE student (
  id INT NOT NULL AUTO_INCREMENT,
  first_name VARCHAR(45) DEFAULT NULL,
  last_name VARCHAR(45) DEFAULT NULL,
  email VARCHAR(45) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
```

**Vérification finale obligatoire :**

```bash
grep ddl-auto src/main/resources/application.properties
# doit afficher : spring.jpa.hibernate.ddl-auto=none

mysql -u springstudent -pspringstudent student_tracker \
  -e "SHOW CREATE TABLE student\G" 2>/dev/null
# doit afficher des VARCHAR(45)
```

---

## 5. Remise à l'état final propre

Après toutes les captures :

```bash
mysql -u springstudent -pspringstudent student_tracker \
  -e "DELETE FROM student; ALTER TABLE student AUTO_INCREMENT = 1;"

mysql -t -u springstudent -pspringstudent student_tracker \
  -e "SELECT COUNT(*) AS total FROM student;" 2>/dev/null
```

Sortie attendue :

```text
+-------+
| total |
+-------+
|     0 |
+-------+
```

Vérifier aussi :

```bash
grep ddl-auto src/main/resources/application.properties
git status --short
```

- `ddl-auto` doit valoir `none` ;
- `target/` ne doit pas apparaître dans `git status` (il est ignoré).

---

## 6. Tableau de suivi des captures

| ID | Fichier suggéré | Contenu | Requis | Fait |
|---|---|---|---|---|
| CAP-01 | `captures/01-create.png` | Console `create`, ids générés 1/2/3 | ✔ | ☐ |
| CAP-02 | `captures/02-select-apres-create.png` | `SELECT` après `create` | ✔ | ☐ |
| CAP-03 | `captures/03-read.png` | Console `read` (findById, findAll, Doe) | ✔ | ☐ |
| CAP-04 | `captures/04-select-avant-update.png` | `SELECT` avant modification | ✔ | ☐ |
| CAP-05 | `captures/05-update.png` | Console `update`, Paul → John | ✔ | ☐ |
| CAP-06 | `captures/06-select-apres-update.png` | `SELECT` après modification | ✔ | ☐ |
| CAP-07 | `captures/07-delete.png` | Console `delete`, id 1 + cas 9999 | ✔ | ☐ |
| CAP-08 | `captures/08-select-apres-delete.png` | `SELECT` après suppression | ✔ | ☐ |
| CAP-09 | `captures/09-logs-sql-insert.png` | `INSERT` + `binding parameter` | ✔ | ☐ |
| CAP-10 | `captures/10-findall.png` | Tri ASC et DESC | ☐ | ☐ |
| CAP-11 | `captures/11-bylastname.png` | `findByLastName("Doe")` | ☐ | ☐ |
| CAP-12 | `captures/12-bulkupdate.png` | `UPDATE` bulk, 2 lignes « Doe » | ☐ | ☐ |
| CAP-13 | `captures/13-named.png` | `@NamedQuery` | ☐ | ☐ |
| CAP-14 | `captures/14-service.png` | Scénario service Alice/Bobby | ☐ | ☐ |
| CAP-15 | `captures/15-deleteall.png` | `Deleted row count` | ☐ | ☐ |
| CAP-16 | `captures/16-select-apres-deleteall.png` | Table vide (`total = 0`) | ☐ | ☐ |
| CAP-17 | `captures/17-action-inconnue.png` | `Action inconnue: inconnu` | ☐ | ☐ |
| CAP-18 | `captures/18-jar-actions.png` | Exécution du jar | ☐ | ☐ |
| CAP-19 | `captures/19-bonus1-ddl.png` | DDL Hibernate `drop/create` | Bonus | ☐ |

---

## 7. Archives de secours (`evidence/`)

Les logs réels de chaque action sont conservés dans `evidence/` ; ils peuvent servir de preuve de
repli si une capture est floue ou incomplète :

| Fichier | Contenu |
|---|---|
| `evidence/create.log` | `create` + SQL `INSERT` + bindings + état de la table |
| `evidence/read.log` | `read` complet |
| `evidence/findall.log` | tris ASC/DESC |
| `evidence/bylastname.log` | recherche par nom |
| `evidence/update.log` | `update` + SQL `UPDATE` + état de la table |
| `evidence/delete.log` | `delete` + cas 9999 + état de la table |
| `evidence/bulkupdate.log` | mise à jour bulk + bindings |
| `evidence/named.log` | requête nommée |
| `evidence/service.log` | scénario service |
| `evidence/deleteall.log` | suppression de masse |
| `evidence/inconnu.log` | action inconnue |
| `evidence/jar-actions.log` | `create`/`read`/`update`/`delete` via le jar |
| `evidence/final-pass.txt` | synthèse du passage final (11 actions PASS, table vide) |
| `evidence/bonus1-backup.sql` | sauvegarde avant les essais `ddl-auto` |

Commande type pour archiver une nouvelle exécution :

```bash
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=create 2>&1 \
  | tee evidence/create.log
```

Relire un log :

```bash
less evidence/create.log
# ou
grep -iE "insert|binding|Saved" evidence/create.log
```

---

## 8. Dépannage rapide

| Problème | Cause probable | Solution |
|---|---|---|
| `Access denied for user 'springstudent'` | Mauvais mot de passe ou utilisateur absent | Vérifier `application.properties` et le script SQL |
| `Unknown database 'student_tracker'` | Base absente | Exécuter `src/main/resources/sql/02-student-tracker.sql` |
| `Table 'student_tracker.student' doesn't exist` | Table absente | Recréer la table (script SQL) |
| `./mvnw: Permission denied` | Wrapper non exécutable | `chmod +x mvnw` |
| `Schema-validation: missing table [student]` | `ddl-auto=validate` et table absente | Recréer la table ou remettre `ddl-auto=none` |
| Aucun log SQL | Niveaux de log modifiés | Vérifier les 3 lignes `logging.level...` |
| Le terminal coupe les lignes | Fenêtre trop étroite | Agrandir la fenêtre ou réduire la police |
| La capture ne montre pas la commande | `clear` ou scroll | Relancer la commande et capturer immédiatement |
| `Table student is empty` après une capture | Action `deleteall` exécutée | Relancer `create` ou la remise à zéro |
