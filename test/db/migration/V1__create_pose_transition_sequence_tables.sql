CREATE TABLE Poses (
  pose_id int(11) NOT NULL AUTO_INCREMENT,
  created_ts timestamp NULL,
  name varchar(256) NOT NULL,
  created_by varchar(256) NULL,
  image_url varchar(256) NULL,
  description_md VARCHAR(10000) CHARACTER SET utf8 NULL,
  PRIMARY KEY (pose_id)
);

CREATE TABLE Transitions (
  transition_id int(11) NOT NULL AUTO_INCREMENT,
  created_ts timestamp NULL,
  name varchar(256) NOT NULL,
  created_by varchar(256) NULL,
  description_md VARCHAR(10000) CHARACTER SET utf8 NULL,
  pose_from int(11) NOT NULL,
  pose_to int(11) NOT NULL,
  PRIMARY KEY (transition_id),
  FOREIGN KEY (pose_from) REFERENCES Poses (pose_id),
  FOREIGN KEY (pose_to) REFERENCES Poses (pose_id)
)