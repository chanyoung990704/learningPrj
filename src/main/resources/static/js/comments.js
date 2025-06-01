document.addEventListener('DOMContentLoaded', function() {
    // 답글 폼 열기
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('btn-reply')) {
            const commentId = e.target.getAttribute('data-comment-id');
            const replyForm = document.getElementById('reply-form-' + commentId);
            const textarea = replyForm.querySelector('textarea');
            const commentAuthor = e.target.getAttribute('data-comment-author');
            // 모든 reply-form 닫기
            document.querySelectorAll('.reply-form').forEach(f => f.style.display = 'none');
            // 열기
            if (replyForm) {
                replyForm.style.display = 'block';
                if (!textarea.value.startsWith('@' + commentAuthor + ' ')) {
                    textarea.value = '@' + commentAuthor + ' ';
                }
                textarea.focus();
            }
            document.querySelectorAll('.edit-form').forEach(f => f.style.display = 'none');
            document.querySelectorAll('.comment-content').forEach(c => c.style.display = 'block');
        }
        // 답글 취소
        if (e.target.classList.contains('btn-cancel-reply')) {
            const commentId = e.target.getAttribute('data-comment-id');
            const replyForm = document.getElementById('reply-form-' + commentId);
            if (replyForm) replyForm.style.display = 'none';
        }
        // 수정 폼 열기
        if (e.target.classList.contains('btn-edit-comment')) {
            const commentId = e.target.getAttribute('data-comment-id');
            const contentElement = document.getElementById('comment-content-' + commentId);
            const editFormElement = document.getElementById('comment-edit-form-' + commentId);
            const textarea = editFormElement.querySelector('textarea');
            document.querySelectorAll('.edit-form').forEach(f => f.style.display = 'none');
            document.querySelectorAll('.comment-content').forEach(c => c.style.display = 'block');
            if (editFormElement && contentElement) {
                let content = contentElement.textContent.trim();
                if (content.startsWith('@')) content = content.substring(content.indexOf(' ') + 1);
                textarea.value = content;
                contentElement.style.display = 'none';
                editFormElement.style.display = 'block';
                textarea.focus();
            }
            document.querySelectorAll('.reply-form').forEach(f => f.style.display = 'none');
        }
        // 수정 취소
        if (e.target.classList.contains('btn-cancel-edit')) {
            const commentId = e.target.getAttribute('data-comment-id');
            const contentElement = document.getElementById('comment-content-' + commentId);
            const editFormElement = document.getElementById('comment-edit-form-' + commentId);
            if (editFormElement && contentElement) {
                editFormElement.style.display = 'none';
                contentElement.style.display = 'block';
            }
        }
    });
});
