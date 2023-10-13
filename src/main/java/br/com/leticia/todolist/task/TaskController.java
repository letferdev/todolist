package br.com.leticia.todolist.task;

import br.com.leticia.todolist.user.IUserRepository;
import br.com.leticia.todolist.utils.Utils;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/")
    public ResponseEntity create(
            @RequestBody TaskModel taskModel,
            HttpServletRequest request){
        var userUuid = (UUID) request.getAttribute("userUuid");
        var user = this.userRepository.findById(userUuid);

        if(!user.isEmpty()) {
            taskModel.setUserUuid(userUuid);

            var currentDate = LocalDateTime.now();

            if(currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Data de inicio anterior a data atual");
            }

            if(taskModel.getStartAt().isAfter(taskModel.getEndAt())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Data de inicio deve ser menor que a data de termino");
            }

            var task = this.taskRepository.save(taskModel);

            return ResponseEntity.status(HttpStatus.CREATED).body(task);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário não encontrado");
    }

    @GetMapping("/")
    public List<TaskModel> listAll(HttpServletRequest request){
        var userUuid = (UUID) request.getAttribute("userUuid");

        return this.taskRepository.findByUserUuid(userUuid);
    }

    @PutMapping("/{task_uuid}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID task_uuid){
        var userUuid = (UUID) request.getAttribute("userUuid");
        var task = this.taskRepository.findById(task_uuid).orElse(null);

        if(task != null) {

            if(!task.getUserUuid().equals(userUuid)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task não pertence ao usuário logado!!");
            }

            Utils.copyNonNullProperties(taskModel, task);

            var save = this.taskRepository.save(task);
            return ResponseEntity.status(HttpStatus.CREATED).body(save);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task não encontrada!!");
    }
}
