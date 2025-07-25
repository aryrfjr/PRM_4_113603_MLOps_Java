# PRM_4_113603_MLOps_Java

The repository [PRM_4_113603_MLOps](https://github.com/aryrfjr/PRM_4_113603_MLOps) contains an ongoing **Python-based** implementation of the **MLOps system** for predicting DFT-level chemical bond strengths (-ICOHP values) in metallic glasses based on the methodology published in Phys. Rev. Materials 4, 113603 (DOI: https://doi.org/10.1103/PhysRevMaterials.4.113603; or the [preprint](https://www.researchgate.net/publication/345634787_Chemical_bonding_in_metallic_glasses_from_machine_learning_and_crystal_orbital_Hamilton_population)). It follows a **GETL (Generate + ETL)** approach combining classical molecular dynamics, DFT simulations, and machine learning (Gaussian Process Regression) with SOAP descriptors and bond distances as features, whose original workflow is available in the repository [PRM_4_113603](https://github.com/aryrfjr/PRM_4_113603). Below is a high-level diagram describing the original pipeline that I developed between 2018 and 2019 in my last post-doc experience:

![MLOPs workflow used in PRM_4_113603](img/PRM_4_113603_MLOps.drawio.png)

The repository [PRM_4_113603_MLOps](https://github.com/aryrfjr/PRM_4_113603_MLOps) includes **data generation**, **feature engineering**, **model training**, **evaluation**, and **human-in-the-loop active learning**, all planned to be orchestrated through **Airflow**, tracked via **MLflow**, and stored in PostgreSQL and MinIO/S3-based stores. The architecture consists of a set of services (**Streamlit**, **FastAPI**, **Airflow**, and **MLflow**) which coordinate the execution of key components such as the **data explorer and augmenter**, the **human-in-the-loop active learning** process, and **feature extraction** pipeline.

The **FastAPI back-end application** serves as the central controller in this architecture, acting as the communication bridge between the **Streamlit** user interface and the services for **workflow orchestration** and **ML lifecycle** management. It handles all user-triggered actions such as launching **data generation**, triggering **model evaluations**, and updating **experiment states**. By exposing a clean REST interface, it enables modular coordination of the MLOps components, facilitating **interactive workflows**, **experiment reproducibility**, and seamless **integration** with other services like **Airflow** and the **Feature Store Lite**.

For building reliable, scalable, and maintainable systems, Java with **Spring Boot** is clearly superior to Python-based frameworks like **FastAPI** (and generally superior to other alternatives like **Flask** or **Django**). It offers a mature, **enterprise-grade platform** with powerful built-in features for **security**, **configuration**, **monitoring**, **scalability**, **maintainability**, and **integration** with additional services like **Kafka**, **Redis**. Additionally, it and simplifies integration with corporate systems such as **SSO (Single Sign-On)**, **LDAP**, **OAuth2** providers, centralized logging and data governance tools; all of which are essential in enterprise environments. By leveraging **Spring Boot**, the **MLOps platform** becomes easier to deploy, audit, secure, and scale across teams, making it a stronger fit for long-term use within an organization.

This repository provides a **Java-based** counterpart for the controller layer that handles all user-triggered interactions (via an **Angular** app) and serves as the central gateway. Built with **Spring Boot**, it exposes orchestration endpoints callable by **Airflow** while delegating domain-specific tasks like **model training** to Python-based services implemented with **FastAPI**, following a **microservice-friendly**, polyglot architecture, described in the following diagram:

![MLOPs system architecture](img/PRM_4_113603_MLOps_JavaPythonArchitecture.drawio.png)

The diagram below illustrates a **SAGA-based orchestration pattern** applied to a two-stage pre-deployment MLOps workflow. Triggered via the Angular frontend, the Spring Boot Gateway coordinates two Airflow DAGs; one for exploring simulation results and another for ETL and model preparation. Each DAG makes synchronous calls to Python microservices (FastAPI) for domain-specific processing steps like SOAP vector extraction and PBSSDB creation. Kafka is used to notify the orchestrator of key state transitions, and compensating actions are defined for failure scenarios to maintain consistency across S3-stored artifacts and HPC job states. This architecture exemplifies a robust, event-driven approach to handling long-running scientific workflows in a modular, polyglot MLOps system.

```mermaid
---
config:
  theme: redux-color
  look: handDrawn
---
sequenceDiagram
    participant UI as Front-end (Angular)
    participant GW as Back-end Gateway API (Spring)
    participant AF1 as DAG PD Explore (Airflow)
    participant AF2 as DAG PD ETL Model (Airflow)
    participant A as HPC (Spring)
    participant C as Extract SOAP vectors (FastAPI)
    participant D as Create PBSSDB (FastAPI)
    participant K as Kafka
    participant S3 as Object Store (S3/MinIO)
    UI->>GW: Pre-Deployment Explore
    GW->>GW: Create SAGA (state = EXPLORE_STARTED)
    GW->>AF1: Trigger DAG PD Explore
    AF1->>A: Submit jobs | POST '/api/v1/jobs'
    A->>S3: Write semi-structured data from CMD/DFT simulations
    AF1->>K: Emit message EXPLORE_RUNS_SUBMITTED
    K->>GW: Receive message EXPLORE_RUNS_SUBMITTED
    GW->>GW: Update Run & SubRun 0 (state = JOBS_SUBMITTED)
    AF1->>A: Check jobs | GET /api/v1/jobs/{job_id}
    AF1->>AF1: Try until the jobs are completed
    AF1->>K: Emit message EXPLORE_RUNS_FINISHED
    K->>GW: Receive message EXPLORE_RUNS_FINISHED
    GW->>GW: Update Run & SubRun 0 (state = JOBS_FINISHED)
    GW->>GW: Update SAGA (state = EXPLORE_FINISHED)
    alt DAG PD Explore fails (Submit/Wait)
        AF1->>K: Emit message EXPLORE_RUNS_FAILED (with SAGA_ID)
        K->>GW: Receive message EXPLORE_RUNS_FAILED (with SAGA_ID)
        GW->>GW: Update Run & SubRun (state = JOBS_FAILED)
        GW->>GW: Update SAGA (state = EXPLORE_FAILED)
    end
    GW->>GW: Update SAGA (state = ETL_STARTED)
    GW->>AF2: Trigger DAG PD ETL Model
    AF2->>C: Extract SOAP vectors | POST /api/v1/dataops/extract_soap_vectors/{nominal_composition}/{run_number}/0
    C->>S3: Store SOAP vectors
    AF2->>AF2: Repeat for all Run numbers
    AF2->>D: Create PBSSDB | POST /api/v1/dataops/create_pbssdb/{nominal_composition}
    D->>S3: Write PBSSDB
    AF2->>K: Emit message EXPLORE_SOAP_VECTORS_EXTRACTED_SSDB_CREATED
    K->>GW: Receive message EXPLORE_SOAP_VECTORS_EXTRACTED_SSDB_CREATED
    GW->>GW: Update SubRun simulation artifact
    GW->>GW: Update saga (state = ETL_COMPLETED)
    alt DAG PD ETL Model (SOAP/PBSSDB)
        AF1->>K: Emit message EXTRACT_SOAP_FAILED || CREATE_SSDB_FAILED (with SAGA_ID)
        K->>GW: Receive message EXTRACT_SOAP_FAILED || CREATE_SSDB_FAILED (with SAGA_ID)
        GW->>GW: Update Run & SubRun (state = EXTRACT_SOAP_FAILED || CREATE_SSDB_FAILED)
        GW->>GW: Update SAGA (state = ETL_FAILED)
        GW->>S3: Cleanup intermediate data (SOAP.vec && PBSSDB dir)
        GW-->>UI: Notify failure
    end
    UI->>GW: Poll for result
    GW-->>UI: Return S3 result
```

## Notes for DEV:

- MLOps REST API documentation: http://localhost:8080/swagger-ui.html

- SimOps REST API documentation: http://localhost:8082/swagger-ui.html

- Microservices (DataOps and ModelOps) REST API documentation: http://localhost:8000/docs

- MinIO console UI: http://localhost:9001

- Angular (Dev Container): http://localhost:4200

- Airflow: http://localhost:8084

- MLflow: http://localhost:5000
