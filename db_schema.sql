CREATE TABLE ARTICLE (
  pmc_id  INTEGER NOT NULL,
  title   TEXT,
  PRIMARY KEY(pmc_id)
);

CREATE TABLE TRAIT_TABLE (
  tab_id  INTEGER PRIMARY KEY AUTOINCREMENT,
  tab_lb TEXT,
  pmc_id  INTEGER NOT NULL,
  FOREIGN KEY(pmc_id) REFERENCES ARTICLE(pmc_id)
);

CREATE TABLE ABBREVIATION (
  abbrev    TEXT NOT NULL,
  expansion TEXT NOT NULL,
  pmc_id    INTEGER NOT NULL,
  FOREIGN KEY(pmc_id) REFERENCES ARTICLE(pmc_id)
);

CREATE TABLE COLUMN_ENTRY (
  col_id  INTEGER PRIMARY KEY AUTOINCREMENT,
  tab_id  INTEGER NOT NULL,
  header  TEXT,
  type    TEXT,
  annot   TEXT,
  FOREIGN KEY(tab_id) REFERENCES TRAIT_TABLE(tab_id)
);

CREATE TABLE CELL_ENTRY (
  row_id  INTEGER NOT NULL,
  col_id  INTEGER NOT NULL,
  value   TEXT,
  FOREIGN KEY(col_id) REFERENCES COLUMN_ENTRY(col_id)
);

CREATE TABLE QTL (
  tab_id            INTEGER NOT NULL,
  row_id            INTEGER NOT NULL,
  trait_in_article  TEXT,
  trait_in_onto     TEXT,
  trait_uri         TEXT,
  chromosome        TEXT,
  marker            TEXT,
  marker_uri        TEXT,
  gene              TEXT,
  gene_uri          TEXT,
  PRIMARY KEY(tab_id, row_id),  	
  FOREIGN KEY(tab_id) REFERENCES TRAIT_TABLE(tab_id)
);

CREATE VIEW V_QTL AS
  SELECT * FROM QTL INNER JOIN TRAIT_TABLE USING(tab_id);
