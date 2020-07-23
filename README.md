# MIDD - An initial implementation
This is a proof-of-concept of the *Metadata Interpretation Driven Development* (MIDD) methodology. For more information read the article (MIDD.pdf) posted in this repository.

The objective of this project is to demonstrate the operation of a software construction methodology (MIDD) that allows the complete reuse of your code to meet the demands of persistence (we could do this with others software concerns, like security) for applications from the most different domains of information systems without any change in code, despite changing data requirements. 

All that is required is the writing of a data model (see session: _usage (0)_) in json format to be loaded at the time of the project's initialization and, in addition, the correct use of HTTP endpoints described as following.

# Audience
Scientists and Engineers Software and Researchers, Software Developers, Students, etc.

# Brief Description of Use

This implementation was built using Eclipse/Maven, Java/Spring-boot and Postgresql. To use it you need know and install this tools (use JRE version 11). 

#### Important:
As this implementation is a proof-of-concept, it has some limitations of synchronization between the representation made in json inserted in dmodel.json (file in the resources folder of the java project) and the representation that the interpreter automatically creates in the database. After the first initialization of the interpreter it creates a representation of the data model described by dmodel.json in the database. From that moment on, some types of changes made to dmodel.json will need to be made manually in the database. Changes such as: removal of attributes or classes, are cases that will require interference from the human operator of the interpreter to synchronize representations (between the dmodel.json file and the database). 

#### Environment Configure and Install

1. You need to install JRE 11 (or later) PostgreSQL 9.6 (or later) 

2. PostgreSQL config (pg_hba.conf):  the _SGDB authentication mode_ is __trust__; port: 5432 (default) 

3. Interpreter config: **Before run jar project compiled by maven**, you need create user and a database with its names defined in _application.properties_ (file in java project resources folder). If you want change this names in _application.properties_ do it. By default, to Library model, you will set postgresql database name as test\_db and user admin\_test\_db.

4. Model (**dmodel.json file**): the following models are to be described in the dmodel.json file. Each model must be run in different interpreter instances, simultaneously. Or if in the same instance, once at a time. Put dmodel.json file in /tmp folder (/tmp/dmodel.json). You can create your own model and changing the dmodel.json file.

5. Service initialize: go to the project folder and run  command: **java -jar midd-0.0.1-SNAPSHOT.jar&**

#### Model to Library Example 

```
{
    "GoodAndService": {
        "_classDef": {
        },
        "name": {
            "type": "String",
            "isUnique": false,
            "isNullable": false,
            "length": 12
        }
    },
    "Book": {
        "_classDef": {
            "_extends": "GoodAndService",
            "_specializationStrategy": "TablePerClass"
        },
        "isbn": {
            "type": "String",
            "isUnique": false,
            "isNullable": true,
            "length": 32
        }
    },
    "Renter": {
        "_classDef": {
        },
        "name": {
            "type": "String",
            "length": 128,
            "isUnique": false,
            "isNullable": false
        },
        "register": {
            "type": "String",
            "length": 64,
            "isUnique": true,
            "isNullable": false
        }
    },
    "Student": {
        "_classDef": {
            "_extends": "Renter",
            "_specializationStrategy": "TablePerClass"
        },
        "birthdate": {
            "type": "Date",
            "isUnique": false,
            "isNullable": false
        }
    },
    "Rental": {
        "_classDef": {
            "_uniqueTuple":  [ "student", "book", "start" ]
        },
        "student": {
            "type": "Student",
            "isUnique": false,
            "isNullable": false
        },
        "book": {
            "type": "Book",
            "isUnique": false,
            "isNullable": false
        },
        "start": {
            "type": "Date",
            "isUnique": false,
            "isNullable": false
        },
        "finish": {
            "type": "Date",
            "isUnique": false,
            "isNullable": true
        }
    }
}
```

*Obs*.: (_1_) when you run the jar, after compiling the java source code with maven, you will have an HTTP API for data persistence through the endpoints described below

### (1) Create Data
##### (1.1) HTTP Message: POST
##### (1.2) URL (Request): http://localhost:8080/{classUID}
##### (1.3) Example: 
```
curl --header "Content-Type: text/plain" \
	--request POST \
	--data '{ "name": "The Brothers Karamazov", "isbn":"1461.535A-234.1231N" }' \
	http://localhost:8080/Book
```

### (2) Read Data
##### (2.1) HTTP Message: GET
##### (2.2) URL (Request): https://localhost:8080/{classUID}/{id}
##### (2.3) Example:
```
curl --request GET http://localhost:8080/Book/1
```

### (3) Update Data
##### (3.1) HTTP Message: PUT
##### (3.2) URL (Request): https://localhost:8080/{classUID}/{id}
##### (3.3) Example: 
```
curl --header "Content-Type: text/plain" \
	--request PUT \
	--data '{ "name": "The Brothers Karamazov" }' \
	http://localhost:8080/Book/1
```

### (4) Delete Data
##### (4.1) HTTP Message: DELETE
##### (4.2) URL (Request): https://localhost:8080/{classUID}/{id}
##### (4.3) Example: 
```
curl --request DELETE http://localhost:8080/Book/1
```
