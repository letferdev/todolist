package br.com.leticia.todolist.task;

import br.com.leticia.todolist.user.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ITaskRepository extends JpaRepository<TaskModel, UUID> {
    List<TaskModel> findByUserUuid(UUID userUuid);
}
