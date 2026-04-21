# 🌳 Famous People Family Tree

An interactive web application for exploring family trees of famous people using real data from Wikidata.  
Users can search individuals, visualize their ancestry, discover relationships, and explore analytics about their lineage.

---

## 🚀 Tech Stack

### 🧠 Backend
- **Java + Spring Boot** – main backend framework
- **Spring Data Neo4j** – integration with graph database
- **GraphQL (Spring for GraphQL)** – flexible API for nested data queries
- **Neo4j** – graph database for storing relationships (family trees)
- **Redis (planned)** – caching frequently accessed data
- **WebClient (Spring)** – fetching data from Wikidata API

---

### 🌐 Frontend
- **Next.js (TypeScript)** – React framework for building UI
- **React Flow / D3.js (planned)** – interactive tree visualization
- **Tailwind CSS** – styling
- **Zustand / Redux Toolkit (planned)** – state management

---

### 🗄️ Data Source
- **Wikidata API** – source of people, relationships, and metadata

---

### 🧪 Testing
- **JUnit** – unit testing (backend)
- **Testcontainers** – integration testing with Neo4j
- **Jest + React Testing Library (planned)** – frontend testing

---

### ⚙️ DevOps & Tooling
- **GitHub** – version control & project management
- **GitHub Actions (planned)** – CI/CD pipelines
- **Docker (planned)** – containerization
- **Vercel (planned)** – frontend deployment
- **Render / Railway (planned)** – backend deployment

---

## 🧩 Core Features

- 🔍 Search for any famous person
- 🌳 Interactive family tree (up to 3 generations)
- 🧑 Detailed person profiles (birthdate, country, photo, Wikipedia link)
- 🔗 Relationship finder between two people
- 📊 Analytics (origin, occupations, universities, etc.)
- 📈 Popularity tracking
- 📤 Export tree as image/PDF

---

## 🏗️ Architecture Overview
Frontend (Next.js)
↓
GraphQL API (Spring Boot)
↓
Neo4j (Graph Database)
↓
Wikidata API (external data source)
## 📚 What This Project Demonstrates

- Graph database modeling (Neo4j)
- Complex relationship queries (ancestors, connections)
- GraphQL API design
- Data integration from external APIs
- Interactive data visualization
- Full-stack application architecture

---

## 🛣️ Roadmap

### Phase 1 - Basic backend
- [ ] Basic backend (Spring Boot + Neo4j)
- [ ] Search person
- [ ] Display 2–3 level family tree

### Phase 2 - Backend API
- [ ] GraphQL API
- [ ] Wikidata integration

### Phase 3 - Frontend(MVP)
- [ ] Interactive UI (expand/collapse tree)
- [ ] Person detail view
- [ ] Popularity tracking

### Phase 4 - Advanced features
- [ ] Relationship finder
- [ ] Advanced analytics
- [ ] Export features (PDF/image)
- [ ] Suggest new relationships/add new sources

### Phase 4.5 - Performance
- [ ] Performance optimizations
- [ ] Caching (Redis)

---

## ⚠️ Challenges

- Handling incomplete or inconsistent Wikidata data
- Optimizing graph queries for performance
- Designing intuitive tree visualization UI
- Managing deep relationship traversal efficiently

---

## 📌 Notes

This project focuses on:
- Graph-based thinking
- Clean backend architecture
- Real-world data handling
- Scalable full-stack design

---

## 👨‍💻 Author

Richard
