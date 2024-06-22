$(document).ready(function() {
    $('#chat-input').keypress(function(event) {
        if (event.which == 13) {
            event.preventDefault();
            var message = $(this).val();
            if (message.trim() !== '') {
                $('#chat-box').append('<div class="chat-message user-message">' + message + '</div>');
                $(this).val('');
                $(this).prop('disabled', true); // Disable the input field

                // Show progress bar
                $('#progress-bar').css('display', 'block');

                // Send message to backend
                $.ajax({
                    url: 'http://localhost:8080/api/chat', // Updated to match your backend endpoint
                    method: 'POST',
                    contentType: 'text/plain',
                    data: message,
                    success: function(response) {
                        $('#progress-bar').css('display', 'none'); // Hide progress bar
                        $('#chat-box').append('<div class="chat-message bot-message">' + response + '</div>');
                        $('#chat-input').prop('disabled', false); // Enable the input field
                        $('#chat-box').scrollTop($('#chat-box')[0].scrollHeight);
                    },
                    error: function() {
                        $('#progress-bar').css('display', 'none'); // Hide progress bar
                        $('#chat-box').append('<div class="chat-message error-message">Error sending message.</div>');
                        $('#chat-input').prop('disabled', false); // Enable the input field
                        $('#chat-box').scrollTop($('#chat-box')[0].scrollHeight);
                    }
                });
            }
        }
    });
});

