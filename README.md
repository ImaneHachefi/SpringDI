# SpringDI – Injection de dépendances avec Spring

Ce TP met en œuvre l'**injection de dépendances** avec le framework **Spring** (`spring-context`). Les dépendances entre `IMetier` et `IDao` sont injectées par le conteneur Spring, de deux façons : par **configuration XML** et par **annotations**, avec la gestion des **profils** (`dev` / `prod`).

## Architecture

```
springDI
├── pom.xml
├── docs/                                   # Captures d'écran
└── src
    ├── main
    │   ├── java/main
    │   │   ├── dao
    │   │   │   ├── IDao.java               # Interface DAO
    │   │   │   ├── DaoImpl.java            # Profil "prod" (valeur 100.0)
    │   │   │   └── DaoImpl2.java           # Profil "dev"  (valeur 150.0)
    │   │   ├── metier
    │   │   │   ├── IMetier.java            # Interface métier
    │   │   │   └── MetierImpl.java         # calcul() = dao.getValue() × 2
    │   │   └── presentation
    │   │       ├── Presentation.java       # Version annotations + profil
    │   │       └── PresentationXML.java    # Version XML
    │   └── resources
    │       └── applicationContext.xml      # Déclaration des beans (version XML)
    └── test/java/main
        └── metier/MetierImplTest.java      # Test unitaire de MetierImpl
```

### Structure du projet

<img width="515" height="738" alt="Capture d&#39;écran 2026-10-15 142054" src="https://github.com/user-attachments/assets/18c455f1-ce08-4d66-bf99-9bdd51e14bf4" />


## Principe

| Élément | Rôle |
|---|---|
| `IDao` / `DaoImpl` / `DaoImpl2` | Couche d'accès aux données, avec deux implémentations selon le profil |
| `IMetier` / `MetierImpl` | Couche métier, qui dépend de `IDao` sans connaître son implémentation |
| `Presentation` / `PresentationXML` | Démarrent le conteneur Spring et récupèrent le bean `metier` |

## Version 1 : Configuration XML

Les beans sont déclarés dans `src/main/resources/applicationContext.xml`. L'injection se fait par le **setter** `setDao` :

```xml
<bean id="dao"  class="main.dao.DaoImpl"></bean>
<bean id="dao2" class="main.dao.DaoImpl2"></bean>

<bean id="metier" class="main.metier.MetierImpl">
    <property name="dao" ref="dao2"></property>
</bean>
```

`PresentationXML` charge ce contexte avec `ClassPathXmlApplicationContext` :

```java
ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
IMetier metier = (IMetier) context.getBean("metier");
System.out.println("Résultat (XML) = " + metier.calcul());
```

Pour changer d'implémentation, il suffit de modifier `ref="dao2"` en `ref="dao"` dans le fichier XML, sans toucher au code Java.

## Version 2 : Annotations et profils

- `@Component` déclare les beans (`dao`, `dao2`, `metier`).
- `@Autowired` + `@Qualifier("dao2")` injecte le DAO dans `MetierImpl`.
- `@Profile("prod")` et `@Profile("dev")` n'activent qu'une implémentation du DAO selon le profil actif.
- `@ComponentScan` indique à Spring les packages à scanner (`main.dao` et `main.metier`).

`Presentation` active le profil **dev** :

```java
AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
context.getEnvironment().setActiveProfiles("dev");
context.register(Presentation.class);
context.refresh();

IMetier metier = context.getBean(IMetier.class);
System.out.println("Résultat = " + metier.calcul());
```

## Prérequis

- **JDK 17 ou supérieur** (exigé par Spring 7)
- Maven
- Connexion Internet au premier lancement (téléchargement de `spring-context`)

Dépendances principales (`pom.xml`) :

| Dépendance | Version |
|---|---|
| `org.springframework:spring-context` | 7.0.9 |
| `junit:junit` (test) | 4.13.2 |

## Exécution

Depuis IntelliJ : clic droit sur `Presentation.java` ou `PresentationXML.java`, puis **Run**.

En ligne de commande :

```bash
mvn compile
mvn exec:java -Dexec.mainClass="main.presentation.Presentation"
mvn exec:java -Dexec.mainClass="main.presentation.PresentationXML"
```

## Résultats de l'exécution

### Version annotations (`Presentation`)

<img width="1888" height="948" alt="Capture d&#39;écran 2026-10-15 142034" src="https://github.com/user-attachments/assets/f4e78124-440a-4b55-b1c3-2bbf54d90d50" />

Sortie attendue :

```
Version de développement
Résultat = 300.0
```

### Version XML (`PresentationXML`)

<img width="1274" height="909" alt="Capture d&#39;écran 2026-10-15 144637" src="https://github.com/user-attachments/assets/967851f3-623f-493b-adf0-892bf67423b3" />

Sortie attendue :

```
Version de développement
Résultat (XML) = 300.0
```

Les deux versions donnent 150.0 × 2 = **300.0**, car elles utilisent `DaoImpl2`.

## Test unitaire

`MetierImplTest` vérifie `MetierImpl` **sans Spring**, en injectant un DAO factice qui renvoie `10.0` :

```java
MetierImpl metier = new MetierImpl();
metier.setDao(dao);                              // injection manuelle
Assert.assertEquals(20.0, metier.calcul(), 0.001); // 10.0 × 2
```

Lancement : `mvn test`

### Capture : résultat des tests

<img width="1918" height="954" alt="Capture d&#39;écran 2026-10-15 143121" src="https://github.com/user-attachments/assets/5380eb9f-24bc-4a14-8cf0-639aba36d82f" />

## Conclusion

Grâce à l'injection de dépendances, `MetierImpl` ne dépend que de l'interface `IDao`. On change d'implémentation (`DaoImpl` ou `DaoImpl2`) uniquement par configuration : le fichier XML, ou le profil Spring actif.
