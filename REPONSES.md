# Réponses — TP 03 : Hibernate & JPA, CRUD avec l'EntityManager

Projet : `cruddemo` — package `com.tp3.cruddemo` — Java 25 — Spring Boot 4.0.x — MySQL `student_tracker`.

L'application est une application en ligne de commande : un `CommandLineRunner` lit `args[0]`
et exécute une action (`create`, `read`, `update`, `delete`, etc.) puis l'application se termine.
Aucun contrôleur REST ni repository Spring Data n'est utilisé : tout passe par `EntityManager`.

Les sorties réelles qui appuient les réponses ci-dessous sont dans le dossier `evidence/`
(`create.log`, `read.log`, `update.log`, `delete.log`, etc.).

---

## 1. JPA, Hibernate et Spring Data JPA : spécification, implémentation, orchestration

| Élément | Rôle | Ce que cela signifie concrètement |
|---|---|---|
| **JPA** (Jakarta Persistence) | **Spécification** | Un ensemble d'interfaces et d'annotations standard : `Entity`, `Id`, `EntityManager`, `TypedQuery`, `@Transactional` n'en fait pas partie (c'est Spring), etc. JPA définit le contrat mais ne fournit aucune implémentation. |
| **Hibernate** | **Implémentation** | Fournisseur ORM qui implémente JPA. C'est lui qui traduit les entités en SQL, gère le cache de premier niveau, le dirty checking, la génération des identifiants, les dialectes MySQL, etc. `EntityManager` est ici une façade sur une `Session` Hibernate. |
| **Spring Data JPA** | **Orchestration / abstraction au-dessus de JPA** | Surcouche Spring qui crée des repositories à partir d'interfaces (`JpaRepository`), dérive les requêtes depuis les noms de méthodes (`findByLastName`), et branche le tout sur un fournisseur JPA. Elle réduit le code répétitif mais n'est pas obligatoire. |

Dans ce TP, la dépendance `spring-boot-starter-data-jpa` est présente, mais **aucun repository
Spring Data n'est utilisé**. Le projet reste volontairement « JPA pur » :

- Spring Boot configure la `DataSource`, l'`EntityManagerFactory` et le `JpaTransactionManager` ;
- Spring injecte un `EntityManager` dans `StudentDAOImpl` ;
- le DAO appelle directement `persist`, `find`, `merge`, `remove`, `createQuery`, etc. ;
- Hibernate est le fournisseur JPA réel qui produit le SQL visible dans les logs
  (`org.hibernate.SQL` et `org.hibernate.orm.jdbc.bind`).

En résumé : **JPA = le contrat, Hibernate = le moteur, Spring Data JPA = un confort
supplémentaire que l'on n'utilise pas ici.**

---

## 2. Pourquoi les écritures sont `@Transactional` et pas les lectures

Les méthodes `save`, `update`, `delete`, `deleteAll` et `updateEmailByLastName` modifient la
base. JPA exige une transaction active pour ces opérations : sans transaction, Hibernate lève
une `TransactionRequiredException` (ou la transaction est ouverte puis refermée immédiatement,
ce qui n'est pas le comportement attendu).

`@Transactional` apporte :

- **atomicité** : plusieurs modifications réussissent ou échouent ensemble ;
- **rollback** automatique sur exception non vérifiée ;
- **flush/commit** au bon moment (les `INSERT`/`UPDATE`/`DELETE` sont envoyés à la base à la fin
  de la transaction) ;
- **cohérence du contexte de persistance** (entités gérées, dirty checking).

Les lectures (`findById`, `findAll`, `findAllOrderByLastNameDesc`, `findByLastName`,
`findByLastNameNamed`) ne changent rien. Un simple `SELECT` peut s'exécuter en autocommit,
sans transaction explicite. Ajouter `@Transactional` partout serait inutile ici et donnerait
l'illusion que les lectures ont besoin d'être transactionnelles.

Nuance : si plusieurs lectures doivent voir un état parfaitement cohérent entre elles, on peut
ouvrir une transaction **read-only** (`@Transactional(readOnly = true)`) au niveau service.
Ce n'est pas nécessaire pour les requêtes unitaires de ce TP.

Exemple observé pour `save` (`evidence/create.log`) :

```text
insert into student (email,first_name,last_name) values (?,?,?)
binding parameter (1:VARCHAR) <- [paul@test.com]
binding parameter (2:VARCHAR) <- [Paul]
binding parameter (3:VARCHAR) <- [Doe]
Saved student. Generated id: 1
```

Le `INSERT` n'apparaît qu'au commit de la transaction ouverte par le proxy Spring de
`StudentDAOImpl.save`.

---

## 3. `persist` vs `merge`

| Critère | `entityManager.persist(entity)` | `entityManager.merge(entity)` |
|---|---|---|
| But | Insérer une **nouvelle** entité | Copier l'état d'une entité (souvent détachée) vers une instance **gérée** ; insertion si l'entité n'existe pas, mise à jour sinon |
| État d'entrée attendu | Entité **transiente** (nouvelle, sans identifiant en base) | Entité **détachée** ou transiente ; peut avoir un identifiant |
| Valeur de retour | `void` — l'objet passé devient géré | Une **instance gérée** (potentiellement différente de celle passée) |
| Identifiant généré | Renseigné sur l'entité après le flush/insert (avec `IDENTITY`, dès l'insert) | Renseigné sur l'instance retournée si insertion |
| Objet passé après l'appel | Devient géré (suivi par le contexte de persistance) | **Reste détaché** ; les modifications ultérieures ne sont pas suivies |
| SQL typique observé | `insert into student (email,first_name,last_name) values (?,?,?)` | `update student set email=?,first_name=?,last_name=? where id=?` |
| Transaction | Obligatoire | Obligatoire |

Points importants :

- `merge` **ne rend pas** l'objet passé géré : il retourne une copie gérée. Il faut donc
  utiliser la valeur de retour si l'on veut continuer à travailler sur l'entité gérée.
- L'objet passé à `merge` reste détaché ; si on le modifie après l'appel, Hibernate ne le sait
  pas et ne générera pas de `UPDATE` pour ces nouvelles modifications.
- Dans ce TP, `update(Student)` appelle `entityManager.merge(theStudent)` puis le runner
  **relit** l'entité avec `findById` pour afficher l'état réel en base. C'est volontaire : cela
  montre que la vérité est en base, pas dans l'objet détaché.
- `persist` sur une entité déjà gérée ne fait rien de plus (elle est déjà suivie).
- Avec une clé générée en `IDENTITY`, `persist` déclenche l'`INSERT` assez tôt pour obtenir
  l'identifiant, d'où l'affichage immédiat de `Generated id: 1`.

---

## 4. JPQL vs SQL

JPQL (Java Persistence Query Language) travaille sur le **modèle objet** :

- `Student` est le **nom de l'entité**, pas le nom de la table ;
- `lastName` est le **nom du champ Java**, pas le nom de la colonne ;
- Hibernate traduit la requête en SQL en utilisant le mapping `@Table` / `@Column`.

Requête JPQL du TP :

```java
entityManager.createQuery("FROM Student WHERE lastName = :theData", Student.class)
```

SQL réellement observé dans `evidence/bylastname.log` :

```text
select s1_0.id,s1_0.email,s1_0.first_name,s1_0.last_name
from student s1_0
where s1_0.last_name=?
binding parameter (1:VARCHAR) <- [Doe]
```

Différences principales :

| JPQL | SQL |
|---|---|
| `Student` (entité) | `student` (table) |
| `lastName` (champ) | `last_name` (colonne) |
| `FROM Student` | `from student s1_0` (alias généré) |
| Portable entre bases | Dialecte propre à la base |
| Connaît les associations et l'héritage | Jointures explicites sur les tables |
| Ne dépend pas du nom physique des colonnes | Dépend du schéma réel |

JPQL est donc plus lisible, plus portable et résistant aux renommages de colonnes. SQL natif
reste utile pour des requêtes spécifiques à MySQL ou des optimisations que JPQL ne permet pas.

---

## 5. Pourquoi les paramètres nommés sont plus sûrs que la concaténation

Avec une concaténation :

```java
String jpql = "FROM Student WHERE lastName = '" + nom + "'";
```

si `nom` vaut `' OR '1'='1`, la requête devient :

```sql
... WHERE last_name = '' OR '1'='1'
```

C'est une **injection JPQL/SQL** : l'utilisateur peut lire, modifier ou supprimer des données
selon les droits de la base. Il faut aussi gérer manuellement les apostrophes, les accents, les
dates et les types, ce qui est source d'erreurs.

Avec un paramètre nommé :

```java
TypedQuery<Student> q = entityManager.createQuery(
        "FROM Student WHERE lastName = :theData", Student.class);
q.setParameter("theData", nom);
```

Hibernate envoie la valeur **séparément** de la requête (via un `PreparedStatement` JDBC) :

```text
where s1_0.last_name=?
binding parameter (1:VARCHAR) <- [Doe]
```

Avantages :

- la valeur est traitée comme une **donnée**, jamais comme du code JPQL ;
- le driver JDBC gère l'échappement et le typage ;
- la requête peut être réutilisée et mise en cache ;
- le code est plus lisible et moins fragile.

Les paramètres nommés ne remplacent pas toutes les bonnes pratiques : il faut aussi valider les
entrées, utiliser un compte MySQL à privilèges limités et ne jamais concaténer des noms de
colonnes ou de tables provenant de l'utilisateur.

---

## 6. Ce qu'apporte `@Repository`

`@Repository` est une annotation de stéréotype Spring. Elle a deux effets :

1. **Détection par le component scanning** : la classe `StudentDAOImpl` est enregistrée comme
   bean Spring et peut être injectée dans le service ou le runner.
2. **Traduction des exceptions** : Spring applique un `PersistenceExceptionTranslationPostProcessor`
   aux beans annotés `@Repository`. Les exceptions spécifiques au fournisseur JPA/Hibernate
   (`PersistenceException`, `ConstraintViolationException`, etc.) sont converties en exceptions
   de la hiérarchie `org.springframework.dao.DataAccessException` :
   `DataIntegrityViolationException`, `DuplicateKeyException`, `EmptyResultDataAccessException`, etc.

Avantages :

- le service ne dépend plus des exceptions Hibernate ;
- le code appelant peut traiter des exceptions communes, indépendantes du fournisseur ;
- on peut changer de fournisseur JPA sans réécrire la gestion d'erreurs.

Sans `@Repository`, les exceptions brutes du provider remontent et le couplage au fournisseur
est plus fort. Dans ce TP, c'est `StudentDAOImpl` qui porte `@Repository`, ce qui est cohérent
avec le fait que c'est la seule classe qui touche à `EntityManager`.

---

## 7. Pourquoi `remove(new Student(5))` ne fonctionne pas

`EntityManager.remove(entity)` exige une **entité gérée** par le contexte de persistance
courant. Une instance créée avec `new Student(5)` est **transiente** : même si son champ `id`
vaut 5, Hibernate ne sait pas qu'elle correspond à une ligne existante. Il ne va pas chercher
la ligne en base tout seul.

Le comportement typique est une exception du type :

```text
java.lang.IllegalArgumentException: Removing a detached instance
com.tp3.cruddemo.entity.Student#5
```

La bonne façon de faire, dans **une même transaction**, est :

```java
Student s = entityManager.find(Student.class, 5);
if (s != null) {
    entityManager.remove(s);
}
```

C'est exactement ce que fait `StudentDAOImpl.delete(Integer id)` :

```java
Student theStudent = entityManager.find(Student.class, id);
if (theStudent == null) {
    return false;          // aucun NPE, aucune exception : rien à supprimer
}
entityManager.remove(theStudent);
return true;
```

Résultat observé (`evidence/delete.log`) :

```text
Deleted student with id 1: true
After delete, findById(1) = null
delete(9999) -> false (no student with this id, nothing deleted)
```

Alternative pour supprimer en masse sans charger les entités : une requête JPQL bulk
`DELETE FROM Student WHERE ...` avec `executeUpdate()` (voir `deleteAll`).

---

## 8. Comportement des valeurs de `spring.jpa.hibernate.ddl-auto`

| Valeur | Comportement | Usage recommandé |
|---|---|---|
| `none` | Hibernate ne touche pas au schéma. Il suppose que les tables existent déjà. | **Production** lorsque le schéma est géré par un DBA, Flyway ou Liquibase. C'est la valeur finale du projet. |
| `validate` | Au démarrage, Hibernate compare le mapping des entités au schéma réel. Aucune modification n'est faite. En cas d'écart (table ou colonne manquante, type incompatible), le démarrage échoue. | **CI / pré-production** pour détecter une dérive entre le code et la base. |
| `update` | Hibernate ajoute les tables et colonnes manquantes, mais ne supprime ni colonne ni donnée. Peut créer une table absente. Les modifications sont implicites et non tracées. | À éviter en production ; éventuellement en développement local. |
| `create` | Hibernate **supprime** les objets existants puis les recrée à chaque démarrage. Toutes les données sont perdues. | Tests et démonstrations uniquement. |
| `create-drop` | Comme `create`, puis supprime le schéma à l'arrêt de l'application. | Tests automatisés uniquement (base jetable). |

Observations réelles (détail dans `docs/bonus1-create-table.txt`) :

- `create` a produit :

  ```text
  drop table if exists student
  create table student (id integer not null auto_increment, email varchar(255),
      first_name varchar(255), last_name varchar(255), primary key (id)) engine=InnoDB
  ```

- `validate` sur le schéma généré : démarrage normal, aucun DDL. Sur une table absente :

  ```text
  SchemaManagementException: Schema validation: missing table [student]
  ```

- `update` sur un schéma conforme : aucun DDL. Sur une table absente : il l'a créée
  (mêmes colonnes `VARCHAR(255)` que `create`), sans `drop table`.
- `create-drop` : `create` au démarrage, puis `drop table if exists student` à l'arrêt
  (observé dans le log du shutdown hook).

**En production : `none` (ou `validate` en contrôle), jamais `create` ni `create-drop`.**
`create` détruit les données à chaque redémarrage ; c'est inacceptable pour une application
réelle. `update` est tentant mais dangereux car il modifie le schéma sans migration versionnée
ni possibilité de rollback.

---

## À comprendre : pourquoi `save` est `@Transactional` mais `findById` ne l'est pas

`save` écrit :

```java
@Transactional
public void save(Student theStudent) {
    entityManager.persist(theStudent);
}
```

Spring crée un proxy autour de `StudentDAOImpl`. L'appel à `save` ouvre une transaction, appelle
`persist`, puis au retour de la méthode Hibernate fait un flush et Spring commit. Sans cette
transaction, l'`INSERT` n'a pas de cadre atomique et JPA refuse l'opération d'écriture.

`findById` lit :

```java
public Student findById(Integer id) {
    return entityManager.find(Student.class, id);
}
```

Aucune transaction n'est nécessaire : `find` exécute un `SELECT` et retourne l'entité (ou
`null`). Il n'y a rien à valider, rien à rollbacker, aucun état à modifier. Le `SELECT` observé
est :

```text
select s1_0.id,s1_0.email,s1_0.first_name,s1_0.last_name from student s1_0 where s1_0.id=?
binding parameter (1:INTEGER) <- [1]
```

Nuance importante : si `findById` est appelé **à l'intérieur** d'une transaction de service
(bonus 3), il rejoint cette transaction (propagation `REQUIRED`). La règle « pas de
`@Transactional` sur les lectures » vaut pour les méthodes de DAO appelées seules ; elle
n'empêche pas un service de regrouper plusieurs lectures et écritures dans une même unité de
travail.

---

## Bonus 1 — Génération du schéma par Hibernate

Le compte rendu complet, les DDL exacts et le tableau comparatif sont dans
**`docs/bonus1-create-table.txt`**.

Résumé :

- sauvegarde préalable des données dans `evidence/bonus1-backup.sql` ;
- `ddl-auto=create` a généré `id INT AUTO_INCREMENT`, mais des colonnes
  `VARCHAR(255)` au lieu des `VARCHAR(45)` du script, car aucune longueur n'est précisée
  dans `@Column` ;
- `validate` échoue proprement si la table manque ;
- `update` crée la table si elle manque, sans la supprimer ;
- retour final à `ddl-auto=none` et table recréée à l'identique du script (`VARCHAR(45)`,
  table vide).

Pour obtenir exactement `VARCHAR(45)`, il faudrait annoter les champs ainsi :

```java
@Column(name = "first_name", length = 45)
private String firstName;
```

---

## Bonus 2 — `@NamedQuery`

La requête nommée est déclarée sur l'entité :

```java
@Entity
@Table(name = "student")
@NamedQuery(name = "Student.findByLastName",
            query = "FROM Student WHERE lastName = :theData")
public class Student { ... }
```

Le DAO l'exécute avec :

```java
TypedQuery<Student> q = entityManager.createNamedQuery("Student.findByLastName", Student.class);
q.setParameter("theData", theLastName);
return q.getResultList();
```

Comparaison avec la requête inline de la question 5.4 :

| Critère | Requête inline (`createQuery`) | `@NamedQuery` |
|---|---|---|
| Emplacement | Dans le DAO, au moment de l'appel | Sur l'entité, déclarée une fois |
| Réutilisation | Dupliquée si utilisée à plusieurs endroits | Réutilisable par nom |
| Validation | À l'exécution | Hibernate parse et valide la requête au démarrage |
| Lisibilité du DAO | La JPQL est mélangée au code | Le DAO ne contient que le nom |
| Souplesse | Idéale pour une requête dynamique ou ponctuelle | Idéale pour une requête stable et partagée |
| SQL généré | Identique | Identique |

Sortie observée (`evidence/named.log`) :

```text
Students with lastName 'Doe' (named query):
select s1_0.id,s1_0.email,s1_0.first_name,s1_0.last_name
from student s1_0 where s1_0.last_name=?
binding parameter (1:VARCHAR) <- [Doe]
Student{id=1, firstName='Paul', lastName='Doe', email='paul@test.com'}
Student{id=3, firstName='Susan', lastName='Doe', email='susan@test.com'}
```

La requête nommée ne change pas le SQL : elle centralise et valide la JPQL plus tôt.

---

## Bonus 3 — Couche service et propagation

`StudentService` / `StudentServiceImpl` s'intercalent entre le runner et le DAO. Le service
expose les opérations CRUD et porte `@Transactional` sur ses méthodes d'écriture. Le DAO
conserve également ses `@Transactional` pour que les exercices de base restent corrects et
évaluables séparément.

Propagation `REQUIRED` (valeur par défaut) :

- le runner appelle `studentService.createTwoAndUpdateFirstName(...)` ;
- Spring ouvre **une** transaction autour de cette méthode de service ;
- les appels internes au DAO (`save`, `update`) passent par les proxies Spring ;
- comme une transaction est déjà active, `REQUIRED` fait **rejoindre** cette transaction au
  DAO : il n'y a pas de nouvelle transaction, donc pas de commit intermédiaire ;
- si une erreur survient après le premier `save`, tout est rollbacké, y compris les insertions
  déjà effectuées.

C'est l'intérêt d'un transaction de service : **plusieurs appels DAO forment une seule unité
de travail atomique**. La transaction au niveau DAO reste utile comme filet de sécurité pour
les appels directs (exercices 5.1 à 5.7), mais elle est trop fine pour exprimer un cas d'usage
métier complet.

Scénario observé (`evidence/service.log`) :

```text
Created via service: Student{id=4, firstName='Alice', lastName='Service', email='alice@service.com'}
Created via service: Student{id=5, firstName='Bobby', lastName='Service', email='bob@service.com'}
Updated via service: Student{id=5, firstName='Bobby', lastName='Service', email='bob@service.com'}
```

Le runner prend à la fois `StudentDAO` (pour les actions du TP) et `StudentService` (pour
l'action `service`). C'est un choix volontaire pour respecter la consigne « le runner prend
`StudentDAO` » tout en démontrant la couche service.

---

## Preuves et fichiers associés

- `evidence/*.log` : sorties réelles de chaque action, avec logs SQL et état de la table ;
- `evidence/jar-actions.log` : test du jar exécutable (`target/cruddemo-0.0.1-SNAPSHOT.jar`) ;
- `evidence/bonus1-backup.sql` : sauvegarde avant les essais `ddl-auto` ;
- `docs/bonus1-create-table.txt` : DDL Hibernate, comparaison et essais validate/update/create-drop ;
- `GUIDE_CAPTURES.md` : procédure détaillée pour produire les captures demandées.
