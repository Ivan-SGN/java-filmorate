CREATE TABLE directors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE film_directors (
    film_id INT NOT NULL,
    director_id INT NOT NULL,

    PRIMARY KEY (film_id, director_id),

    CONSTRAINT fk_film_directors_film
        FOREIGN KEY (film_id)
        REFERENCES films(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_film_directors_director
        FOREIGN KEY (director_id)
        REFERENCES directors(id)
        ON DELETE CASCADE
);