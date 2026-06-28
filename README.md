# Compte Rendu — Injection des Dépendances (IoC)

**Module :** Architecture JEE et Systèmes Distribués  
**Projet :** BDCC-IOC  
**Auteur :** Mohamed CHATR
**Technologies :** Java 21, Spring Framework 7.0.6, Maven

---

## Objectif

L'objectif de ce TP est de comprendre et mettre en œuvre le principe d'**Inversion de Contrôle (IoC)** et d'**Injection de Dépendances (DI)** à travers plusieurs approches, allant de l'instanciation manuelle jusqu'à l'utilisation du framework Spring.

---

## Architecture du Projet

```
net.mohamed/
├── dao/
│   ├── IDao.java           → Interface DAO
│   └── DaoImpl.java        → Implémentation base de données
├── ext/
│   └── DaoImplV2.java      → Implémentation capteurs (version 2)
├── metier/
│   ├── IMetier.java        → Interface métier
│   └── MetierImpl.java     → Implémentation métier (couplage faible)
└── pres/
    ├── Pres1.java              → Instanciation statique
    ├── pres2.java              → Instanciation dynamique (réflexion)
    ├── PresSpringXML.java      → Spring version XML
    └── PresSpringAnnotation.java → Spring version annotations
```

---

## Étape 1 — Interface `IDao`

L'interface `IDao` définit le contrat de la couche d'accès aux données avec une méthode `getData()` qui retourne une valeur de type `double`.

```java
// src/main/java/net/mohamed/dao/IDao.java
package net.mohamed.dao;

public interface IDao {
    double getData();
}
```

---

## Étape 2 — Implémentations de `IDao`

### `DaoImpl` — Version base de données

Première implémentation simulant un accès à une base de données. Annotée `@Component("d")` pour la détection par Spring.

```java
// src/main/java/net/mohamed/dao/DaoImpl.java
@Component("d")
public class DaoImpl implements IDao {
    @Override
    public double getData() {
        System.out.println("Version base de données");
        return 34;
    }
}
```

### `DaoImplV2` — Version capteurs

Deuxième implémentation simulant une source de données capteurs. Annotée `@Component("d2")`.

```java
// src/main/java/net/mohamed/ext/DaoImplV2.java
@Component("d2")
public class DaoImplV2 implements IDao {
    @Override
    public double getData() {
        System.out.println("Version capteurs ....");
        return 13;
    }
}
```

---

## Étape 3 — Interface `IMetier`

L'interface `IMetier` définit le contrat de la couche métier avec une méthode `calcul()`.

```java
// src/main/java/net/mohamed/metier/IMetier.java
package net.mohamed.metier;

public interface IMetier {
    double calcul();
}
```

---

## Étape 4 — Implémentation de `IMetier` avec couplage faible

`MetierImpl` dépend de `IDao` (interface) et non d'une implémentation concrète. Cela garantit le **couplage faible** : on peut changer l'implémentation du DAO sans modifier la couche métier.

La classe expose un constructeur, un setter et utilise `@Autowired` + `@Qualifier` pour les trois modes d'injection.

```java
// src/main/java/net/mohamed/metier/MetierImpl.java
@Component("metier")
public class MetierImpl implements IMetier {

    @Autowired
    @Qualifier("d2")
    private IDao dao;

    public MetierImpl(IDao dao) { this.dao = dao; }
    public MetierImpl() {}
    public void setDao(IDao dao) { this.dao = dao; }

    @Override
    public double calcul() {
        double t = dao.getData();
        return t * 12 * Math.PI / 2 * Math.cos(t);
    }
}
```

---

## Étape 5 — Injection des dépendances

### 5a. Instanciation statique

Les objets sont créés manuellement dans le code. La dépendance est injectée via le **constructeur**.

```java
// src/main/java/net/mohamed/pres/Pres1.java
public class Pres1 {
    public static void main(String[] args) {
        DaoImplV2 d = new DaoImplV2();
        MetierImpl metier = new MetierImpl(d);
        System.out.println("RES= " + metier.calcul());
    }
}
```

**Inconvénient :** le code est fermé à la modification — changer d'implémentation du DAO impose de recompiler la classe de présentation.

---

### 5b. Instanciation dynamique

Les noms des classes à instancier sont lus depuis un fichier `config.txt`. L'instanciation se fait par **réflexion Java**, ce qui rend le code ouvert à l'extension sans recompilation.

**Fichier `config.txt` :**
```
net.mohamed.ext.DaoImplV2
net.mohamed.metier.MetierImpl
```

```java
// src/main/java/net/mohamed/pres/pres2.java
public class pres2 {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(new File("config.txt"));

        Class cDao = Class.forName(scanner.nextLine());
        IDao d = (IDao) cDao.newInstance();

        Class cMetier = Class.forName(scanner.nextLine());
        IMetier metier = (IMetier) cMetier.getConstructor(IDao.class).newInstance(d);

        System.out.println("RES=" + metier.calcul());
    }
}
```

**Avantage :** changer d'implémentation se fait en modifiant uniquement `config.txt`, sans toucher au code source.

---

### 5c. Spring — Version XML

Spring gère l'instanciation et l'injection via un fichier de configuration XML. L'injection du DAO dans le métier est réalisée par **setter** (`<property>`).

**Fichier `src/main/resources/config.xml` :**
```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="d" class="net.mohamed.ext.DaoImplV2"/>

    <bean id="metier" class="net.mohamed.metier.MetierImpl">
        <property name="dao" ref="d"/>
    </bean>

</beans>
```

```java
// src/main/java/net/mohamed/pres/PresSpringXML.java
public class PresSpringXML {
    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("config.xml");
        IMetier metier = ctx.getBean(IMetier.class);
        System.out.println("RES=" + metier.calcul());
    }
}
```

---

### 5c. Spring — Version Annotations

Spring scanne le package `net.mohamed` à la recherche des classes annotées (`@Component`) et injecte automatiquement les dépendances grâce à `@Autowired`. Le qualificatif `@Qualifier("d2")` précise quelle implémentation de `IDao` utiliser.

```java
// src/main/java/net/mohamed/pres/PresSpringAnnotation.java
public class PresSpringAnnotation {
    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext("net.mohamed");
        IMetier metier = ctx.getBean(IMetier.class);
        System.out.println("RES=" + metier.calcul());
    }
}
```

| Annotation | Rôle |
|---|---|
| `@Component("id")` | Déclare un bean Spring avec un identifiant |
| `@Autowired` | Injection automatique de la dépendance |
| `@Qualifier("d2")` | Sélectionne l'implémentation `DaoImplV2` parmi plusieurs candidats |

---

## Synthèse comparative

| Approche | Couplage | Flexibilité | Recompilation nécessaire |
|---|---|---|---|
| Instanciation statique | Fort | Faible | Oui |
| Instanciation dynamique | Faible | Moyenne | Non |
| Spring XML | Faible | Haute | Non |
| Spring Annotations | Faible | Haute | Non |

---

## Dépendances Maven (`pom.xml`)

```xml
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-core</artifactId>
        <version>7.0.6</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>7.0.6</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-beans</artifactId>
        <version>7.0.6</version>
    </dependency>
</dependencies>
```

---

## Conclusion

Ce TP illustre l'évolution naturelle vers l'Inversion de Contrôle :

1. L'**instanciation statique** couple fortement le code et impose des recompilations à chaque changement.
2. L'**instanciation dynamique** par réflexion découple l'instanciation du code, mais nécessite d'implémenter soi-même le mécanisme d'IoC.
3. **Spring** automatise entièrement ce mécanisme, que ce soit via XML (configuration explicite) ou via annotations (configuration implicite par scan). Le couplage faible est garanti par l'utilisation d'interfaces (`IDao`, `IMetier`), rendant le système extensible sans modification du code existant.
