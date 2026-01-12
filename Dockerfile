# =========================
# 1) Build Frontend
# =========================
FROM node:20-alpine AS fe-build
WORKDIR /fe

COPY branchmaster-fe/package*.json ./
RUN npm ci

COPY branchmaster-fe/ ./
RUN npm run build

# =========================
# 2) Frontend Runtime (SPA fallback, NO nginx)
# =========================
FROM node:20-alpine AS frontend
WORKDIR /app

RUN npm install -g serve

# Copy entire FE project output (so we don't fail if dist/build doesn't exist)
COPY --from=fe-build /fe ./_fe_out

EXPOSE 5173

CMD sh -lc '\
  if [ -d "./_fe_out/dist" ] && [ -f "./_fe_out/dist/index.html" ]; then \
    echo "Serving frontend from dist/"; \
    serve -s ./_fe_out/dist -l 5173; \
  elif [ -d "./_fe_out/build" ] && [ -f "./_fe_out/build/index.html" ]; then \
    echo "Serving frontend from build/"; \
    serve -s ./_fe_out/build -l 5173; \
  else \
    echo "ERROR: No dist/ or build/ directory found in frontend output"; \
    ls -la ./_fe_out; \
    exit 1; \
  fi \
'


# =========================
# 3) Build Backend
# =========================
FROM maven:3.9.9-eclipse-temurin-21 AS be-build
WORKDIR /be

COPY branchmaster/pom.xml ./
COPY branchmaster/src ./src

RUN mvn -q -DskipTests package

# =========================
# 4) Backend Runtime
# =========================
FROM eclipse-temurin:21-jre AS backend
WORKDIR /app

COPY --from=be-build /be/target/*.jar /app/app.jar

ENV JAVA_OPTS=""
EXPOSE 8080

CMD ["sh", "-lc", "exec java $JAVA_OPTS -jar /app/app.jar"]
