CREATE TABLE directors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE film_directors (
    film_id INT,
    director_id INT,
    PRIMARY KEY (film_id, director_id)
);