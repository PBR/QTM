CREATE TABLE ARTICLE (
  pmc_id  NUMERIC NOT NULL,
  title   TEXT,
  PRIMARY KEY(pmc_id)
);

CREATE TABLE TRAIT_TABLE (
  tab_id  NUMERIC NOT NULL,
  pmc_id  NUMERIC NOT NULL,
  PRIMARY KEY(tab_id),
  FOREIGN KEY(pmc_id) REFERENCES ARTICLE(pmc_id)
);

CREATE TABLE ABBREVIATION (
  abbrev    TEXT NOT NULL,
  expansion TEXT NOT NULL,
  pmc_id    NUMERIC NOT NULL,
  FOREIGN KEY(pmc_id) REFERENCES ARTICLE(pmc_id)
);

CREATE TABLE COLUMN_ENTRY (
  col_id  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  tab_id  NUMERIC NOT NULL,
  header  TEXT,
  type    TEXT,
  annot   TEXT,
  FOREIGN KEY(tab_id) REFERENCES TRAIT_TABLE(tab_id)
);

CREATE TABLE CELL_ENTRY (
  row_id  NUMERIC NOT NULL,
  col_id  NUMERIC NOT NULL,
  value   TEXT,
  type    TEXT,
  PRIMARY KEY (row_id, col_id),
  FOREIGN KEY(col_id) REFERENCES COLUMN_ENTRY(col_id)
);

CREATE TABLE QTL (
  pmc_id            NUMERIC NOT NULL,
  tab_id            NUMERIC NOT NULL,
  row_id            NUMERIC NOT NULL,
  trait_in_article  TEXT,
  trait_in_onto     TEXT,
  trait_uri         TEXT,
  chromosome        TEXT,
  marker            TEXT,
  marker_uri        TEXT,
  gene              TEXT,
  gene_uri          TEXT,
  PRIMARY KEY(pmc_id, tab_id, row_id),
  FOREIGN KEY(pmc_id) REFERENCES ARTICLE(pmc_id),
  FOREIGN KEY(tab_id) REFERENCES TRAIT_TABLE(tab_id)
);
