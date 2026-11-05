# cruddemo — TP 03 : Hibernate & JPA

Application Spring Boot 4.0.x (Java 25, Maven Wrapper) illustrant un CRUD avec **JPA pur** :
`EntityManager`, requêtes JPQL, transactions Spring. Aucun repository Spring Data, aucune
dépendance supplémentaire, pas de Lombok.

## Prérequis

- Java 25 (`java -version`)
- MySQL 8 en service
- Le wrapper Maven fourni (`./mvnw`)
- Base `student_tracker`, utilisateur `springstudent` / `springstudent`

## Mise en place de la base

Le script de référence contient la base, l'utilisateur, les droits et la table :

```bash
mysql -u root -p < src/main/resources/sql/02-student-tracker.sql
```

Si la base et l'utilisateur existent déjà, il suffit de créer la table :

```bash
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

Configuration utilisée (`src/main/resources/application.properties`) :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/student_tracker
spring.datasource.username=springstudent
spring.datasource.password=springstudent
spring.jpa.hibernate.ddl-auto=none
```

`ddl-auto=none` : le schéma est géré par le script SQL, jamais par Hibernate.

## Lancer une action

L'application est un `CommandLineRunner` : elle lit `args[0]`, exécute l'action puis se termine.

```bash
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=create
```

| Action | Effet |
|---|---|
| `create` | Insère Paul Doe, Mary Public et Susan Doe ; affiche les ids générés |
| `read` | Relit le premier étudiant par `findById`, affiche `findAll()` puis les « Doe » |
| `findall` | Affiche la liste triée par `lastName` ASC puis DESC |
| `bylastname` | Affiche les étudiants dont le nom est `Doe` (requête inline) |
| `named` | Même recherche via la `@NamedQuery` |
| `update` | Passe le prénom du premier étudiant à `John` (`merge`) |
| `delete` | Supprime le premier étudiant, puis teste `delete(9999)` sans NPE |
| `deleteall` | Supprime toutes les lignes (`executeUpdate`) |
| `bulkupdate` | Met à jour les emails des « Doe » en une requête bulk |
| `service` | Scénario via `StudentService` : crée deux étudiants puis en renomme un |
| action inconnue | Affiche `Action inconnue: <action>` |

Sans argument, l'action par défaut est `read`.

### Jar exécutable

```bash
./mvnw -q package -DskipTests
java -jar target/cruddemo-0.0.1-SNAPSHOT.jar create
java -jar target/cruddemo-0.0.1-SNAPSHOT.jar read
```

### Remise à zéro de la table

```bash
mysql -u springstudent -pspringstudent student_tracker \
  -e "DELETE FROM student; ALTER TABLE student AUTO_INCREMENT = 1;"
```

## Structure du projet

```text
src/main/java/com/tp3/cruddemo/
├── CruddemoApplication.java        # main + CommandLineRunner (actions)
├── dao/
│   ├── StudentDAO.java             # interface du DAO
│   └── StudentDAOImpl.java         # seul accès à EntityManager (@Repository)
├── entity/
│   └── Student.java                # entité JPA + @NamedQuery
└── service/
    ├── StudentService.java         # couche service (bonus 3)
    └── StudentServiceImpl.java     # @Service, transactions d'écriture
src/main/resources/
├── application.properties
└── sql/02-student-tracker.sql      # script de référence (non auto-exécuté)
docs/bonus1-create-table.txt        # comparaison schéma SQL vs Hibernate
evidence/                           # logs réels des exécutions
captures/                           # captures d'écran (voir GUIDE_CAPTURES.md)
```

## Documentation

- **[REPONSES.md](REPONSES.md)** : réponses aux 8 questions de compréhension et aux bonus.
- **[GUIDE_CAPTURES.md](GUIDE_CAPTURES.md)** : procédure détaillée des captures d'écran.
- **[docs/bonus1-create-table.txt](docs/bonus1-create-table.txt)** : DDL Hibernate et comparaison.

## Notes

- Les écritures sont `@Transactional` (DAO et service), les lectures ne le sont pas.
- Le DAO conserve ses `@Transactional` pour que les exercices de base restent autonomes ;
  la transaction de service englobe plusieurs appels DAO via la propagation `REQUIRED`.
- Les logs SQL sont activés via `org.hibernate.SQL=debug` et
  `org.hibernate.orm.jdbc.bind=trace`.
