_.templateSettings = interpolate : /\{\{(.+?)\}\}/g

class Upload
  constructor: ( el ) ->
    @el = el
    @img = @el.find('#upload')
    @id = @img.attr('rel')
    @comments = []
    @commenter = new CommentForm( @, $('#new-comment'),
                                  @img.offset() )

    @img.click ( e ) =>
      offset = @el.offset()
      @commenter.show( e.pageX - offset.left,
                       e.pageY - offset.top )

    @el.on 'clear', =>
      @el.find('.comment').hide()
      @commenter.hide()

    @fetchComments()
  commentsPath: ->
    "/comments/#{@id}"
  postComment: ( comment ) ->
    $.post @commentsPath(), comment: comment, ( r ) => @addComment( JSON.parse(r) )
  fetchComments: ->
    $.getJSON @commentsPath(), ( comments ) => @addComment(c) for c in comments
  addComment: ( comment ) ->
    new Comment( @, comment )

class CommentForm
  constructor: ( upload, form, offset ) ->
    @upload = upload
    @el = form
    @offset = offset
    @el.on 'submit', _.bind( @submit, @ )
    @marker = new Marker( @upload )
    @marker.hide()
    @upload.el.append @marker.el
  show: ( x, y ) ->
    @upload.el.trigger('clear')
    @el.show()
    opts = top: y, left: x, opacity: 1.0
    @marker.show( x, y )
    opts.left -= w if ((w = @el.outerWidth()) + opts.left) > ($(window).width() * 0.9)
    @x = opts.left
    @y = opts.top
    @el.animate( opts, 200 )
    @el.find('textarea').focus()
  hide: ->
    @marker.hide()
    @el.hide()
  submit: ( e ) ->
    e.preventDefault()
    e.stopPropagation()
    @marker.hide()
    @el.hide()
    text = @el.find('textarea').val()
    if text && text.length > 0
      @upload.postComment( x: @x, y: @y, text: text )
    @el.css( opacity: 0 )
    @el.find('textarea').val('')
    return false

class Marker
  template: _.template( "<div rel='{{_id}}' class='marker'></div>" )
  constructor: ( comment, id=null ) ->
    @el = $(@template( _id: id || 'new-comment' ))
    @el.click( -> comment.show() ) if id
  hide: ->
    @el.hide()
  show: ( x, y ) ->
    @el.show().css( top: y, left: x )

class Comment
  template: _.template( """
    <div rel='{{_id}}' class='comment'>
      <a href='javascript:void(0);' class='close'>&times;</a>
      <pre>{{text}}</pre>
    </div>
  """ )
  constructor: ( upload, props ) ->
    return unless props.text && props.text.length > 0
    @upload = upload.el
    @props = props
    @props.x = parseInt( @props.x )
    @props.y = parseInt( @props.y )

    @el = $(@template(@props))
    @upload.append @el

    @marker = new Marker( @, @props._id )
    @upload.append @marker.el
    @marker.show( @props.x, @props.y )

    @el.on 'click', 'a.close', => @el.hide()
  show: ->
    @upload.trigger('clear')
    @el.show().css
      opacity: 1,
      top: @props.y
      left: @props.x

$(document).ready ->
  return unless $('#upload-box').length > 0
  new Upload( $('#upload-box') )
